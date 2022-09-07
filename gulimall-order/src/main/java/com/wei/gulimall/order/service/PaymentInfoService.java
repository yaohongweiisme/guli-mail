package com.wei.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.common.utils.PageUtils;
import com.wei.gulimall.order.entity.PaymentInfoEntity;

import java.util.Map;

/**
 * 支付信息表
 *
 * @author wei
 * @email 2558939179qq@gmail.com
 * @date 2022-09-05 15:59:37
 */
public interface PaymentInfoService extends IService<PaymentInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

