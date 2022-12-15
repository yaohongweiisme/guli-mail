package com.wei.gulimall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {

/*    1、全文检索：skuTitle-》keyword
        2、排序：saleCount（销量）、hotScore（热度分）、skuPrice（价格）
        3、过滤：hasStock、skuPrice区间、brandId、catalog3Id、attrs
        4、聚合：attrs                */

//    keyword=小米&sort=saleCount_desc/asc&hasStock=0/1&skuPrice=400_1900&brandId=1&catalog3Id=1&attrs=1_3G:4G:5G&attrs=2_骁龙845&attrs=4_高清屏

    private String keyword;
    private Long catalog3Id;
    private String sort;
    private Integer hasStock=1;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;
    private Integer pageNum=1;

}
