package com.wei.gulimall.ware.dao;

import com.wei.gulimall.ware.entity.WareInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库信息
 * 
 * @author wei
 * @email 2558939179qq@gmail.com
 * @date 2022-09-05 16:07:55
 */
@Mapper
public interface WareInfoDao extends BaseMapper<WareInfoEntity> {

    Long getSkuStock(Long skuId);
}
