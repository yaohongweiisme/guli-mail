package com.wei.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.common.utils.PageUtils;
import com.wei.gulimall.coupon.entity.SeckillSkuRelationEntity;

import java.util.Map;

/**
 * 秒杀活动商品关联
 *
 * @author wei
 * @email 2558939179qq@gmail.com
 * @date 2022-09-05 15:17:58
 */
public interface SeckillSkuRelationService extends IService<SeckillSkuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

