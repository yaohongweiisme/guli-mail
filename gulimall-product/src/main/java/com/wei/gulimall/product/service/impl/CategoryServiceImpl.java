package com.wei.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.common.utils.PageUtils;
import com.wei.common.utils.Query;
import com.wei.gulimall.product.dao.CategoryDao;
import com.wei.gulimall.product.entity.CategoryEntity;
import com.wei.gulimall.product.service.CategoryBrandRelationService;
import com.wei.gulimall.product.service.CategoryService;
import com.wei.gulimall.product.vo.Catelog2Vo;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

//    private Map<String,Object> cache=new HashMap<>();  本地缓存不适用于分布式系统

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //先查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //组装成父子的树形结构
        //level1
        List<CategoryEntity> level1list = entities.stream().filter((categoryEntity) ->
                        categoryEntity.getParentCid() == 0
                ).peek((menu) -> menu.setChildren(getChildren(menu, entities)))
                .sorted((m1, m2) -> {
                    return (m1.getSort() == null ? 0 : m1.getSort()) - (m2.getSort() == null ? 0 : m2.getSort());
                })
                .collect(Collectors.toList());

        return level1list;
    }

    @Override
    public void removeMenuById(List<Long> asList) {
        // TODO 检查菜单关联性，是否被其他地方引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();//目录路径： 父子孙
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }
//    @Caching(
//            evict = {
//                    @CacheEvict(value = {"Category"},key = "'getLevelOne'"),
//                    @CacheEvict(value = {"Category"},key = "'getCatalogLevelTwoAndThreeJson'")
//            }
//    )
    @CacheEvict(value = {"Category"},allEntries = true)
    @Override
    public void updateCascade(CategoryEntity category) {
        updateById(category);
        categoryBrandRelationService.updateCategoryData(category.getCatId(), category.getName());
    }
    @Cacheable(value = {"Category"},key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevelOne() {

        List<CategoryEntity> categoryEntities = baseMapper.selectList(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getParentCid, 0));

        return categoryEntities;
    }


    @Override
    @Cacheable(value = {"Category"},key = "#root.method.name")
    public Map<String, List<Catelog2Vo>> getCatalogLevelTwoAndThreeJson() {
//        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
//        if (StringUtils.isEmpty(catalogJson)) {
//            log.debug("缓存不命中，查询数据库");
            Map<String, List<Catelog2Vo>> fromDb = getCatalogLevelTwoAndThreeJsonFromDbWithRedissonLock();
            return fromDb;
//        }
//        log.debug("缓存命中，直接返回");
//        Map<String, List<Catelog2Vo>> map = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
//        });
//        return fromDb;
    }

    public Map<String, List<Catelog2Vo>> getCatalogLevelTwoAndThreeJsonFromDbWithRedissonLock() {
        RLock lock = redissonClient.getLock("CatalogLevelTwoAndThreeJson-Lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> fromDb;
        try {
            fromDb = getCatalogLevel2And3FromDb();
        } finally {
            lock.unlock();
        }

        return fromDb;

    }


    public Map<String, List<Catelog2Vo>> getCatalogLevelTwoAndThreeJsonFromDbWithRedisLock() {
        //占分布式锁,考虑突发情况，建立key时就设置过期时间
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            log.debug("获取分布式锁成功");
            Map<String, List<Catelog2Vo>> fromDb;      //如果害怕业务时间太长，可以考虑把锁续期或者设置过期时间长些
            try {
                fromDb = getCatalogLevel2And3FromDb();
            } finally {
                //lua脚本解锁key,保证原子性
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                Long execute = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class)
                        , Arrays.asList("lock"), uuid
                );
            }

            //不能直接删除锁，因为设置了过期时间有可能把其他线程的锁也删掉了,我们可以通过uuid设置各自的锁
//            String lockValue = stringRedisTemplate.opsForValue().get("lock");
//            if(lockValue.equals(uuid)){
//                stringRedisTemplate.delete("lock");
//            }

            return fromDb;
        } else {
            //加锁失败，以自旋的方式重试
            log.debug("获取锁失败，等待锁。。。。。。。。。。。。。。。");
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                System.out.println(e);
            }

            return getCatalogLevelTwoAndThreeJsonFromDbWithRedisLock();
        }


    }

    @Nullable
    @Cacheable(value = {"Category"},key = "#root.method.name")
    public Map<String, List<Catelog2Vo>> getCatalogLevel2And3FromDb() {
//        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
//        if (!StringUtils.isEmpty(catalogJson)) {
//            Map<String, List<Catelog2Vo>> map = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
//            });
//            return map;
//        }
        log.debug("查询数据库");
        // 查出所有分类数据，并缓存数据，避免频繁查询数据库
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> levelOne = getListByParent_id(selectList, 0L);

        Map<String, List<Catelog2Vo>> map = levelOne.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                    List<CategoryEntity> level2list = getListByParent_id(selectList, v.getCatId());
                    List<Catelog2Vo> catelog2Vos = null;
                    if (level2list != null) {
                        catelog2Vos = level2list.stream().map(item2 -> {
                            Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, item2.getCatId().toString(), item2.getName());
                            //找三级分类
                            List<CategoryEntity> category3Entities = getListByParent_id(selectList, item2.getCatId());
                            if (category3Entities != null) {
                                List<Catelog2Vo.Catelog3Vo> catelog3VoList = category3Entities.stream().map(item3 -> new Catelog2Vo.Catelog3Vo(item3.getParentCid().toString(), item3.getCatId().toString(), item3.getName()))
                                        .collect(Collectors.toList());
                                catelog2Vo.setCatalog3List(catelog3VoList);
                            }
                            return catelog2Vo;
                        }).collect(Collectors.toList());
                    }
                    return catelog2Vos;
                }
        ));

//        catalogJson = JSON.toJSONString(map);
//        stringRedisTemplate.opsForValue().set("catalogJson", catalogJson, 1, TimeUnit.DAYS);
        return map;
    }


//    public Map<String, List<Catelog2Vo>> getCatalogLevelTwoAndThreeJsonFromDbWithLocalLock() {
//
//            synchronized (this){
//                String catalogJson= stringRedisTemplate.opsForValue().get("catalogJson");
//                if(!StringUtils.isEmpty(catalogJson)){
//                    Map<String, List<Catelog2Vo>> map = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
//                    });
//                    return map;
//                }
//                log.debug("查询数据库");
//                // 查出所有分类数据，并缓存数据，避免频繁查询数据库
//                List<CategoryEntity> selectList = baseMapper.selectList(null);
//
//                List<CategoryEntity> levelOne = getListByParent_id(selectList,0L);
//
//                Map<String, List<Catelog2Vo>> map = levelOne.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
//                            List<CategoryEntity> level2list = getListByParent_id(selectList,v.getCatId());
//                            List<Catelog2Vo> catelog2Vos = null;
//                            if (level2list != null) {
//                                catelog2Vos = level2list.stream().map(item2 -> {
//                                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, item2.getCatId().toString(), item2.getName());
//                                    //找三级分类
//                                    List<CategoryEntity> category3Entities = getListByParent_id(selectList,item2.getCatId());
//                                    if (category3Entities != null) {
//                                        List<Catelog2Vo.Catelog3Vo> catelog3VoList = category3Entities.stream().map(item3 -> new Catelog2Vo.Catelog3Vo(item3.getParentCid().toString(), item3.getCatId().toString(), item3.getName()))
//                                                .collect(Collectors.toList());
//                                        catelog2Vo.setCatalog3List(catelog3VoList);
//                                    }
//                                    return catelog2Vo;
//                                }).collect(Collectors.toList());
//                            }
//                            return catelog2Vos;
//                        }
//                ));
//
//                catalogJson = JSON.toJSONString(map);
//                stringRedisTemplate.opsForValue().set("catalogJson",catalogJson,1, TimeUnit.DAYS);
//                return map;
//            }
//
//    }

    private List<CategoryEntity> getListByParent_id(List<CategoryEntity> selectList, Long parent_cid) {
        List<CategoryEntity> categoryEntities = selectList.stream().filter(item -> item.getParentCid() == parent_cid)
                .collect(Collectors.toList());
        return categoryEntities;
    }


    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if (categoryEntity.getParentCid() != 0) {
            findParentPath(categoryEntity.getParentCid(), paths);
        }
        return paths;
    }

    /*
    前一个参数是需要找子类的分类，后一个参数是所有分类，从所有分类中找
     */
    public List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> childrenList = all.stream().filter(categoryEntity -> Objects.equals(categoryEntity.getParentCid(), root.getCatId()))
                .peek(categoryEntity -> categoryEntity.setChildren(getChildren(categoryEntity, all)))
                .sorted((m1, m2) -> {
                    return (m1.getSort() == null ? 0 : m1.getSort()) - (m2.getSort() == null ? 0 : m2.getSort());
                })
                .collect(Collectors.toList());

        return childrenList;
    }

}