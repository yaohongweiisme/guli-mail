package com.wei.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.common.utils.PageUtils;
import com.wei.gulimall.ware.entity.PurchaseEntity;
import com.wei.gulimall.ware.vo.MergeVo;

import java.util.Map;

/**
 * 采购信息
 *
 * @author wei
 * @email 2558939179qq@gmail.com
 * @date 2022-09-05 16:07:55
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnReceivePurchase(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);
}

