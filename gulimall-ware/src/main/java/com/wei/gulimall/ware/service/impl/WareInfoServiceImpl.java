package com.wei.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wei.gulimall.ware.vo.HasStockVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.common.utils.PageUtils;
import com.wei.common.utils.Query;

import com.wei.gulimall.ware.dao.WareInfoDao;
import com.wei.gulimall.ware.entity.WareInfoEntity;
import com.wei.gulimall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareInfoEntity> wrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.eq(WareInfoEntity::getId, key)
                    .or()
                    .like(WareInfoEntity::getName, key)
                    .or()
                    .like(WareInfoEntity::getAddress,key)
                    .or()
                    .like(WareInfoEntity::getAreacode,key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<HasStockVo> getSkuStock(List<Long> skuIds) {
        List<HasStockVo> hasStockVos = skuIds.stream().map(sku -> {
            HasStockVo hasStockVo = new HasStockVo();
            Long count=baseMapper.getSkuStock(sku);
            if(count==null){
                hasStockVo.setHasStock(false);
            }else{
                hasStockVo.setHasStock(count>0);
            }
            hasStockVo.setSkuId(sku);
            return hasStockVo;
        }).collect(Collectors.toList());
        log.debug("库存列表:"+ hasStockVos);
        return hasStockVos;
    }
}