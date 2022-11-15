package com.wei.gulimall.product.feign;

import com.wei.common.to.HasStockVo;
import com.wei.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {
    @PostMapping("/ware/wareinfo/hasStock")
    List<HasStockVo> getSkuStock(@RequestBody List<Long> skuIds);
}
