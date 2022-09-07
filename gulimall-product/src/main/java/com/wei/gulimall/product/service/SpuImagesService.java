package com.wei.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.common.utils.PageUtils;
import com.wei.gulimall.product.entity.SpuImagesEntity;

import java.util.Map;

/**
 * spu图片
 *
 * @author wei
 * @email 2558939179qq@gmail.com
 * @date 2022-09-05 12:23:49
 */
public interface SpuImagesService extends IService<SpuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

