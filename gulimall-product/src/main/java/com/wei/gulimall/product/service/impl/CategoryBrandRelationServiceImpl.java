package com.wei.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wei.gulimall.product.dao.BrandDao;
import com.wei.gulimall.product.dao.CategoryDao;
import com.wei.gulimall.product.entity.BrandEntity;
import com.wei.gulimall.product.entity.CategoryEntity;
import com.wei.gulimall.product.vo.BrandVo;
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

import com.wei.gulimall.product.dao.CategoryBrandRelationDao;
import com.wei.gulimall.product.entity.CategoryBrandRelationEntity;
import com.wei.gulimall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    @Autowired
    private BrandDao brandDao;
    @Autowired
    private CategoryDao categoryDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());
        save(categoryBrandRelation);
    }

    @Override
    public void updateBrandData(Long brandId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandId(brandId);
        categoryBrandRelationEntity.setBrandName(name);
        update(categoryBrandRelationEntity,new UpdateWrapper<CategoryBrandRelationEntity>()
                .eq("brand_id",brandId));
    }

    @Override
    public void updateCategoryData(Long catId, String name) {
        baseMapper.updateCategoryData(catId,name);
    }

    @Override
    public List<BrandVo> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> relationEntities = list(new LambdaQueryWrapper<CategoryBrandRelationEntity>()
                .eq(catId != null, CategoryBrandRelationEntity::getCatelogId,
                        catId));
        List<BrandVo> brandVos = relationEntities.stream().map(
                r -> {
                    BrandVo brandVo = new BrandVo();
                    brandVo.setBrandId(r.getBrandId());
                    brandVo.setBrandName(r.getBrandName());
                    return brandVo;
                }
        ).collect(Collectors.toList());
        return brandVos;
    }

}