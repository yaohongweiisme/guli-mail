package com.wei.gulimall.search.vo;

import com.wei.common.to.es.EsSkuModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {
        private List<EsSkuModel>  products;

        private Integer currentPageNum;

        private  Long total;

        private Integer totalPages;

        private List<BrandVo> brands;

        private List<CatalogVo> catalogs;

        private List<AttrVo> attrs;
        @Data
        public static class  BrandVo{
                private Long brandId;
                private String brandName;
                private String brandImg;
        }

        @Data
        public static class  CatalogVo{
                private Long catalogId;
                private String catalogName;
        }

        @Data
        public static  class AttrVo{
                private Long attrId;
                private String attrName;
                private List<String>  attrValue;
        }


}
