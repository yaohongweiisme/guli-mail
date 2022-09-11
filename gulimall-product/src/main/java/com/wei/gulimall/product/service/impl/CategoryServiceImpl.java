package com.wei.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.common.utils.PageUtils;
import com.wei.common.utils.Query;

import com.wei.gulimall.product.dao.CategoryDao;
import com.wei.gulimall.product.entity.CategoryEntity;
import com.wei.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

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
        ).peek((menu)-> menu.setChildren(getChildren(menu,entities)))
                .sorted((m1,m2)->{
                    return (m1.getSort()==null?0:m1.getSort()) - (m2.getSort()==null?0:m2.getSort());
                })
                .collect(Collectors.toList());

        return level1list;
    }

    @Override
    public void removeMenuById(List<Long> asList) {
        // TODO 检查菜单关联性，是否被其他地方引用
        baseMapper.deleteBatchIds(asList);
    }

    /*
    前一个参数是需要找子类的分类，后一个参数是所有分类，从所有分类中找
     */
    public List<CategoryEntity> getChildren(CategoryEntity root,List<CategoryEntity> all){
        List<CategoryEntity> childrenList = all.stream().filter(categoryEntity -> Objects.equals(categoryEntity.getParentCid(), root.getCatId()))
                .peek(categoryEntity -> categoryEntity.setChildren(getChildren(categoryEntity, all)))
                .sorted((m1,m2)->{
                    return (m1.getSort()==null?0:m1.getSort()) - (m2.getSort()==null?0:m2.getSort());
                })
                .collect(Collectors.toList());

        return childrenList;
    }

}