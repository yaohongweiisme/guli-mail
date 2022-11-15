package com.wei.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wei.common.constant.ProductConstant;
import com.wei.common.to.HasStockVo;
import com.wei.common.to.SkuReductionTo;
import com.wei.common.to.SpuBoundTo;
import com.wei.common.to.es.EsSkuModel;
import com.wei.common.utils.R;
import com.wei.gulimall.product.entity.*;
import com.wei.gulimall.product.feign.CouponFeignService;
import com.wei.gulimall.product.feign.EsFeignService;
import com.wei.gulimall.product.feign.WareFeignService;
import com.wei.gulimall.product.service.*;
import com.wei.gulimall.product.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.common.utils.PageUtils;
import com.wei.common.utils.Query;

import com.wei.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SpuImagesService spuImagesService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    EsFeignService esFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //保存spu基本信息，pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        this.saveBaseSpuInfo(spuInfoEntity);
        //保存spu的描述图片，pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);
        //保存spu的图片集，pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);
        //保存spu的规格参数，pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntityList = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(attrEntity.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(spuInfoEntity.getId());
            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttrValue(productAttrValueEntityList);
        //5) 保存积分信息，sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo boundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, boundTo);
        boundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(boundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        //保存spu对应的所有sku信息
        //1）sku的基本信息，pms_sku_info
        List<Skus> skus = vo.getSkus();
        if (skus.size() != 0 && skus != null) {
            skus.forEach(item -> {
                String defaultImage = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImage = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuInfoService.saveSkuInfo(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();
                //2）sku的图片信息，pms_sku_images
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setSkuId(skuId);
                            skuImagesEntity.setImgUrl(img.getImgUrl());
                            skuImagesEntity.setDefaultImg(img.getDefaultImg());
                            return skuImagesEntity;
                        }).filter(entity -> {
                            //返回false的会被过滤掉
                            return !StringUtils.isEmpty(entity.getImgUrl());
                        })
                        .collect(Collectors.toList());
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuImagesService.saveBatch(imagesEntities);
                //3）sku的销售属性信息，pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> saleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity saleAttrValue = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, saleAttrValue);
                    saleAttrValue.setSkuId(skuId);
                    return saleAttrValue;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(saleAttrValueEntities);
                //4）sku的优惠、满减等信息，跨库操作(gulimall-sms)-> sms_sku_ladder（打折表）-> sms_sku_full_reduction（满减表）-> sms_member_price（会员价格表）
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || (skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1)) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存spu优惠卷信息失败");
                    }
                }

            });
        }


    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<SpuInfoEntity> wrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            //and ( xxxxxx or xxxxx)
            wrapper.and(w -> {
                w.eq(SpuInfoEntity::getId, key).or().like(SpuInfoEntity::getSpuName, key);
            });
        }
        String status = (String) params.get("status");
        wrapper.eq(!StringUtils.isEmpty(status), SpuInfoEntity::getPublishStatus, status);
        String brandId = (String) params.get("brandId");
        wrapper.eq(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId), SpuInfoEntity::getBrandId, brandId);
        String categoryId = (String) params.get("categoryId");
        wrapper.eq(!StringUtils.isEmpty(categoryId) && !"0".equalsIgnoreCase(categoryId), SpuInfoEntity::getCatalogId, categoryId);

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        List<EsSkuModel> skuModels;
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        //查询当前sku可以被检索的规格信息
        List<ProductAttrValueEntity> baseAttrListForSpu = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = baseAttrListForSpu.stream().map(attr -> attr.getAttrId()).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrs(attrIds);
        HashSet<Long> searchAttrIdsSet = new HashSet<>(searchAttrIds);
        List<EsSkuModel.Attrs> attrsList = baseAttrListForSpu.stream().filter(
                        item -> searchAttrIdsSet.contains(item.getAttrId())
                )
                .map(item -> {
                    EsSkuModel.Attrs attrs1 = new EsSkuModel.Attrs();
                    BeanUtils.copyProperties(item, attrs1);
                    return attrs1;
                })
                .collect(Collectors.toList());
        //todo 发送远程调用，查看库存
        Map<Long, Boolean> stockMap = null;
        try {

            List<HasStockVo> skuStock = wareFeignService.getSkuStock(skuIdList);
            System.out.println("从远程调用获得的库存vo"+skuStock);
            stockMap = skuStock.stream().collect(Collectors.toMap(HasStockVo::getSkuId,
                    item -> item.getHasStock()));
            log.debug("库存map:"+stockMap);
        } catch (Exception e) {
            log.error("库存服务有异常"+ e);
        }

        Map<Long, Boolean> finalStockMap = stockMap;
        skuModels = skus.stream().map(sku -> {
            EsSkuModel esModel = new EsSkuModel();
            BeanUtils.copyProperties(sku, esModel);
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());
            //设置库存
            if (finalStockMap == null) {
                esModel.setHasStock(true);
//                log.error("库存map获取有异常");
            } else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }
            //todo 热度评分
            esModel.setHotScore(0L);
            //查询品牌名和图片
            BrandEntity brand = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brand.getName());
            esModel.setBrandImg(brand.getLogo());
            //查询分类名和信息
            CategoryEntity category = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(category.getName());
            esModel.setAttrs(attrsList);
            return esModel;
        }).collect(Collectors.toList());
        log.debug("skuModel列表:"+skuModels);
        R r = esFeignService.productUp(skuModels);
        if(r.getCode()==0){
            //成功上架，修改状态
            baseMapper.updateStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else {
            //远程调用失败
            log.error("远程调用失败");
        }
    }


}