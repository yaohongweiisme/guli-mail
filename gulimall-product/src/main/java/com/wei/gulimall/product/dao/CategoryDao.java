package com.wei.gulimall.product.dao;

import com.wei.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author wei
 * @email 2558939179qq@gmail.com
 * @date 2022-09-05 12:23:49
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
