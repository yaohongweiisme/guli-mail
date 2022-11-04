package com.wei.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wei.common.constant.WareConstant;
import com.wei.gulimall.ware.entity.PurchaseDetailEntity;
import com.wei.gulimall.ware.service.PurchaseDetailService;
import com.wei.gulimall.ware.service.WareSkuService;
import com.wei.gulimall.ware.vo.MergeVo;
import com.wei.gulimall.ware.vo.PurchaseDoneVo;
import com.wei.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    @Autowired
    public WareSkuService wareSkuService;
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
    public boolean mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        List<Long> items = mergeVo.getItems();
        if(purchaseId==null){
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        //确认采购单状态码是0，1才能合并
        PurchaseEntity byId = getById(purchaseId);
        if(byId.getStatus()==WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                byId.getStatus()==WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()){
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
            return true;
        }
        else{
             return false;
        }

    }

    @Override
    @Transactional
    public void received(List<Long> ids) {
        //确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> purchaseEntities = ids.stream().map(id -> {
            PurchaseEntity byId = getById(id);
            return byId;
        }).filter(purchaseEntity -> {
             if (purchaseEntity.getStatus()==WareConstant.PurchaseStatusEnum.CREATED.getCode()
                    || purchaseEntity.getStatus()==WareConstant.PurchaseStatusEnum.ASSIGNED.getCode() ){
                 return true;
             }
             else
                 return false;
                })
                //改变采购单状态
                .map( purchaseEntity ->
                {
                    purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
                    return purchaseEntity;
                })
                .collect(Collectors.toList());
        updateBatchById(purchaseEntities);
        //采购需求表中采购状态变为正在采购
        purchaseEntities.forEach((item)->{
            List<PurchaseDetailEntity>  purchaseDetailEntities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> purchaseDetailEntityList = purchaseDetailEntities.stream().map(purchaseDetail-> {
                purchaseDetail.setStatus(WareConstant.PurchaseDetailStatusEnum.PURCHASING.getCode());
                return purchaseDetail;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(purchaseDetailEntityList);
        });

    }
    @Transactional
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        Long id = purchaseDoneVo.getId();

        //改变采购项状态
        Boolean flag=true;
        List<PurchaseItemDoneVo> items = purchaseDoneVo.getItems();
        ArrayList<PurchaseDetailEntity> waitToUpdates = new ArrayList<>();
        for(PurchaseItemDoneVo item:items){
            PurchaseDetailEntity detail = new PurchaseDetailEntity();
            if(item.getStatus()==WareConstant.PurchaseDetailStatusEnum.ERROR.getCode()){
                flag=false;
                detail.setStatus(item.getStatus());
            }else{
                detail.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //把成功采购的采购项入库  need skuId、wareId and skuNumber
                PurchaseDetailEntity detailEntity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(detailEntity.getSkuId(),detailEntity.getWareId()
                ,detailEntity.getSkuNum());
            }
            detail.setPurchaseId(id);
            detail.setId(item.getItemId());
            waitToUpdates.add(detail);
        }
        purchaseDetailService.updateBatchById(waitToUpdates);

        //改变采购单状态,以采购项信息为准
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag? WareConstant.PurchaseStatusEnum.FINISH.getCode()
                :WareConstant.PurchaseStatusEnum.HAS_ERROR.getCode());
        updateById(purchaseEntity);
    }

}