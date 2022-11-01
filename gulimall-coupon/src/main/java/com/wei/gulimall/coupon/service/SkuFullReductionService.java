package com.wei.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.common.to.SkuReductionTo;
import com.wei.common.utils.PageUtils;
import com.wei.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author wei
 * @email 2558939179qq@gmail.com
 * @date 2022-09-05 15:17:58
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

