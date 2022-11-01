package com.wei.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wei.common.constant.WareConstant;
import com.wei.gulimall.ware.entity.PurchaseDetailEntity;
import com.wei.gulimall.ware.service.PurchaseDetailService;
import com.wei.gulimall.ware.vo.MergeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.common.utils.PageUtils;
import com.wei.common.utils.Query;

import com.wei.gulimall.ware.dao.PurchaseDao;
import com.wei.gulimall.ware.entity.PurchaseEntity;
import com.wei.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    public PurchaseDetailService purchaseDetailService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnReceivePurchase(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseEntity::getStatus,0)
                .or()
                .eq(PurchaseEntity::getStatus,1);
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }
    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        List<Long> items = mergeVo.getItems();
        if(purchaseId==null){
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntities = items.stream().map(i -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(i);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return detailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailEntities);
        //假如采购单不为空
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        updateById(purchaseEntity);

    }

}