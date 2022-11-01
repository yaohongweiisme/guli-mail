package com.wei.gulimall.product.feign;

import com.wei.common.to.SkuReductionTo;
import com.wei.common.to.SpuBoundTo;
import com.wei.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo boundTo);
    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
