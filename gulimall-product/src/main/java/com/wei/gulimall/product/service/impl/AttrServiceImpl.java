package com.wei.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wei.common.constant.ProductConstant;
import com.wei.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.wei.gulimall.product.dao.AttrGroupDao;
import com.wei.gulimall.product.dao.CategoryDao;
import com.wei.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.wei.gulimall.product.entity.AttrGroupEntity;
import com.wei.gulimall.product.entity.CategoryEntity;
import com.wei.gulimall.product.service.CategoryService;
import com.wei.gulimall.product.vo.AttrRespVo;
import com.wei.gulimall.product.vo.AttrVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.common.utils.PageUtils;
import com.wei.common.utils.Query;

import com.wei.gulimall.product.dao.AttrDao;
import com.wei.gulimall.product.entity.AttrEntity;
import com.wei.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    private AttrGroupDao attrGroupDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private CategoryService categoryService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveAttrDetail(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        save(attrEntity);
        //保存关联关系
        if (attrVo.getAttrType()==ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()&&attrVo.getAttrGroupId()!=null) {        //1是基本属性，0是销售属性
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }

    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(catelogId != 0, "catelog_id", catelogId);
        wrapper.eq("attr_type", "base".equalsIgnoreCase(attrType) ?
                ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() :
                ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = page(new Query<AttrEntity>().getPage(params), wrapper);
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> list = page.getRecords();
        List<AttrRespVo> respVos = list.stream().map(attrEntity ->
        {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            AttrAttrgroupRelationEntity relation = attrAttrgroupRelationDao.selectOne(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                    .eq(attrEntity.getAttrId() != null, AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId()));
            if (relation != null) {
                Long attrGroupId = relation.getAttrGroupId();
                if (attrGroupId != null&&attrEntity.getAttrId() != null) {
                    AttrGroupEntity groupEntity = attrGroupDao.selectById(attrGroupId);
                    attrRespVo.setGroupName(groupEntity.getAttrGroupName());
                }

            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrRespVo.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }

            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrDetail(Long attrId) {
        AttrRespVo respVo = new AttrRespVo();
        AttrEntity attrEntity = getById(attrId);
        BeanUtils.copyProperties(attrEntity, respVo);
        //基本属性设置分组信息
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.selectOne(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                    .eq(AttrAttrgroupRelationEntity::getAttrId, attrId));
            if (relationEntity != null) {
                respVo.setAttrGroupId(relationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                if (attrGroupEntity != null) {
                    respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        //设置分类路径
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        respVo.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (categoryEntity != null) {
            respVo.setCatelogName(categoryEntity.getName());
        }
        return respVo;
    }

    @Override
    @Transactional
    public void updateAttrDetail(AttrVo attrVo) {
        AttrEntity attrEntity = getById(attrVo.getAttrId());
        BeanUtils.copyProperties(attrEntity, attrVo);
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
        relationEntity.setAttrId(attrVo.getAttrId());
        Long count = attrAttrgroupRelationDao.selectCount(new LambdaUpdateWrapper<AttrAttrgroupRelationEntity>()
                .eq(AttrAttrgroupRelationEntity::getAttrId, attrVo.getAttrId()));
        if (count > 0) {
            attrAttrgroupRelationDao.update(relationEntity, new LambdaUpdateWrapper<AttrAttrgroupRelationEntity>()
                    .eq(AttrAttrgroupRelationEntity::getAttrId, attrVo.getAttrId()));
        } else {
            attrAttrgroupRelationDao.insert(relationEntity);
        }
    }

    @Override
    public List<AttrEntity> getRelatedAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                .eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrgroupId));
        List<AttrEntity> attrEntities = relationEntities.stream().map((obj) -> {
            Long attrId = obj.getAttrId();
            AttrEntity attrEntity = this.getById(attrId);
            return attrEntity;
        }).collect(Collectors.toList());
        return attrEntities;
    }

    @Override
    public PageUtils getNoRelatedAttr(Map<String, Object> params, Long attrgroupId) {
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //当前分组只能关联自己分类下的属性,同时属性不能已经关联了其他分组
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(new LambdaQueryWrapper<AttrGroupEntity>()
                .eq(AttrGroupEntity::getCatelogId, catelogId));
        List<Long> ids = attrGroupEntities.stream().map(obj -> {
            return obj.getAttrGroupId();
        }).collect(Collectors.toList());
        List<Long> attrIds = attrAttrgroupRelationDao.selectList(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                        .in(ids!=null&&ids.size()!=0,AttrAttrgroupRelationEntity::getAttrGroupId, ids))
                .stream().map(obj -> {
                    return obj.getAttrId();
                }).collect(Collectors.toList());
        LambdaQueryWrapper<AttrEntity> wrapper = new LambdaQueryWrapper<AttrEntity>()
                .eq(AttrEntity::getCatelogId, catelogId)
                .eq(AttrEntity::getAttrType,ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())
                .notIn(attrIds.size()!=0&&attrIds!=null,AttrEntity::getAttrId, attrIds);
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key) ){
            wrapper.and(w->{
                w.eq(AttrEntity::getAttrId,key).or().like(AttrEntity::getAttrName,key);
            });
        }
        IPage<AttrEntity> page = page(new Query<AttrEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public List<Long> selectSearchAttrs(List<Long> attrIds) {

        return baseMapper.selectSearchableAttrIds(attrIds);
    }


}