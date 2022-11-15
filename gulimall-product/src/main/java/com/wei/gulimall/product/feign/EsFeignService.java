package com.wei.gulimall.product.feign;

import com.wei.common.to.es.EsSkuModel;
import com.wei.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-search")
public interface EsFeignService {
    @PostMapping("/search/productUp")
    R productUp(@RequestBody List<EsSkuModel> skuModels);


}