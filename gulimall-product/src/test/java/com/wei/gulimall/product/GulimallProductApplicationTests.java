package com.wei.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wei.gulimall.product.entity.BrandEntity;
import com.wei.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;
    @Test
    void contextLoads() {
        BrandEntity brandEntity=new BrandEntity();
//        brandEntity.setName("一加");
//        brandEntity.setBrandId(13L);
//        brandEntity.setDescript("oppo子品牌");
//        brandService.updateById(brandEntity);
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>());
        list.forEach(System.out::println);
//        System.out.println("保存成功....");
    }

}
