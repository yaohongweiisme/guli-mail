package com.wei.gulimall.search.controller;

import com.wei.common.excption.BizCodeEnume;
import com.wei.common.to.es.EsSkuModel;
import com.wei.common.utils.R;
import com.wei.gulimall.search.service.ProductUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
public class EsController {
    @Autowired
    ProductUpService productUpService;

    @PostMapping("/productUp")
    public R productUp(@RequestBody List<EsSkuModel> esSkuModels) {
        boolean b;
        try {
            b = productUpService.StatusUp(esSkuModels);
        } catch (Exception e) {
            System.out.println("es上架商品错误" + e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (b) {
            return R.ok();
        } else return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
    }

}
