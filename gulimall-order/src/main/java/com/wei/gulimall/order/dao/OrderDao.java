package com.wei.gulimall.order.dao;

import com.wei.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author wei
 * @email 2558939179qq@gmail.com
 * @date 2022-09-05 15:59:37
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
