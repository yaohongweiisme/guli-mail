package com.wei.gulimall.coupon.service.impl;

import ch.qos.logback.classic.spi.EventArgUtil;
import com.wei.common.to.MemberPrice;
import com.wei.common.to.SkuReductionTo;
import com.wei.gulimall.coupon.entity.MemberPriceEntity;
import com.wei.gulimall.coupon.entity.SkuLadderEntity;
import com.wei.gulimall.coupon.service.MemberPriceService;
import com.wei.gulimall.coupon.service.SkuLadderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.common.utils.PageUtils;
import com.wei.common.utils.Query;

import com.wei.gulimall.coupon.dao.SkuFullReductionDao;
import com.wei.gulimall.coupon.entity.SkuFullReductionEntity;
import com.wei.gulimall.coupon.service.SkuFullReductionService;

@Slf4j
@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {
    @Autowired
    private SkuLadderService skuLadderService;
    @Autowired
    private MemberPriceService memberPriceService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        log.info("来看看skuReductionTo:"+skuReductionTo==null?"空":skuReductionTo.toString());
        //4）sku的优惠、满减等信息，跨库操作(gulimall-sms)-> sms_sku_ladder（打折表）-> sms_sku_full_reduction（满减表）-> sms_member_price（会员价格表）
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());

        if(skuReductionTo.getFullCount()>0){
            skuLadderService.save(skuLadderEntity);
        }

        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo,skuFullReductionEntity);
        if((skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal("0"))==1)){
            save(skuFullReductionEntity);
        }
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
//        log.error( memberPrice==null? " memberPrice是空的" :memberPrice.toString());
        List<MemberPriceEntity> priceEntities = memberPrice.stream().map(m -> {
            MemberPriceEntity priceEntity = new MemberPriceEntity();
            priceEntity.setSkuId(skuReductionTo.getSkuId());
            priceEntity.setMemberLevelId(m.getId());
            priceEntity.setMemberLevelName((m.getName()));
            priceEntity.setMemberPrice(m.getPrice());
            priceEntity.setAddOther(1);
            return priceEntity;
        }).filter(item ->{
                   return (item.getMemberPrice().compareTo(new BigDecimal("0")))==1;
                })
                .collect(Collectors.toList());
        memberPriceService.saveBatch(priceEntities);

    }

}