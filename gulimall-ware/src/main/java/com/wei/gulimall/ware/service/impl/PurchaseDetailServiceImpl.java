package com.wei.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.common.utils.PageUtils;
import com.wei.common.utils.Query;

import com.wei.gulimall.ware.dao.PurchaseDetailDao;
import com.wei.gulimall.ware.entity.PurchaseDetailEntity;
import com.wei.gulimall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseDetailEntity> wrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq(PurchaseDetailEntity::getPurchaseId,key)
                        .or()
                        .eq(PurchaseDetailEntity::getSkuId,key);
            });
        }
        String status = (String) params.get("status");
        wrapper.eq(!StringUtils.isEmpty(status),PurchaseDetailEntity::getStatus,status);
        String wareId = (String) params.get("wareId");
        wrapper.eq(!StringUtils.isEmpty(wareId),PurchaseDetailEntity::getWareId,wareId);
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        List<PurchaseDetailEntity> purchaseDetailEntities = list(new LambdaQueryWrapper<PurchaseDetailEntity>()
                .eq(PurchaseDetailEntity::getPurchaseId, id));

        return purchaseDetailEntities;
    }

}