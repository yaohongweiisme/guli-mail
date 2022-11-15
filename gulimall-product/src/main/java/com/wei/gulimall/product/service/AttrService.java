package com.wei.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.common.utils.PageUtils;
import com.wei.gulimall.product.entity.AttrEntity;
import com.wei.gulimall.product.vo.AttrRespVo;
import com.wei.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author wei
 * @email 2558939179qq@gmail.com
 * @date 2022-09-04 23:26:24
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttrDetail(AttrVo attrVo);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType);

    AttrRespVo getAttrDetail(Long attrId);

    void updateAttrDetail(AttrVo attrVo);

    List<AttrEntity> getRelatedAttr(Long attrgroupId);

    PageUtils getNoRelatedAttr(Map<String, Object> params, Long attrgroupId);

    List<Long> selectSearchAttrs(List<Long> attrIds);
}

