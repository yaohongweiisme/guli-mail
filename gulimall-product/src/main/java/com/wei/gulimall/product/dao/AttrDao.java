package com.wei.gulimall.product.dao;

import com.wei.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author wei
 * @email 2558939179qq@gmail.com
 * @date 2022-09-04 23:26:24
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<Long> selectSearchableAttrIds(@Param("attrIds") List<Long> attrIds);
}
