package com.wei.gulimall.search.service.impl;

import com.alibaba.fastjson2.JSON;
import com.wei.common.to.es.EsSkuModel;
import com.wei.gulimall.search.config.EsConfig;
import com.wei.gulimall.search.constant.EsConstant;
import com.wei.gulimall.search.service.MallSearchService;
import com.wei.gulimall.search.vo.SearchParam;
import com.wei.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResult search(SearchParam param) {
        SearchResult result = null;
        SearchRequest searchRequest = getSearchRequest(param);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, EsConfig.COMMON_OPTIONS);
            result = buildSearchResult(searchResponse, param);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();
        SearchHits hits = response.getHits();
        List<EsSkuModel> esSkuModels = new ArrayList<>();

        //封装查到的产品信息
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                EsSkuModel esSkuModel = JSON.parseObject(sourceAsString, EsSkuModel.class);
                if(!StringUtils.isEmpty(param.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String stringForSkuTitle = skuTitle.getFragments()[0].string();
                    esSkuModel.setSkuTitle(stringForSkuTitle);
                }

                esSkuModels.add(esSkuModel);
            }
        }
        result.setProducts(esSkuModels);

        //封装属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            long attrId = bucket.getKeyAsNumber().longValue();
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_Name_agg")).getBuckets().get(0).getKeyAsString();
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_Value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = item.getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());

            attrVo.setAttrId(attrId);
            attrVo.setAttrValue(attrValues);
            attrVo.setAttrName(attrName);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);


        //从聚合结果封装有关产品的品牌、分类、价格信息
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();

        ParsedLongTerms catalogAgg = response.getAggregations().get("catalog_agg");
        List<? extends Terms.Bucket> buckets = catalogAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.valueOf(keyAsString));

            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalog_name);

            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);

            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        //封装结果总数、总页数、当前页码
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        int pages = (int) (total % EsConstant.PRODUCT_DEFAULT_PAGE_SIZE);
        int totalPages = pages == 0 ? pages : (pages + 1);
        result.setTotalPages(totalPages);
        int currentPageNum = param.getPageNum();
        result.setCurrentPageNum(currentPageNum);


        return null;
    }

    @NotNull
    private static SearchRequest getSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //过滤
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        if (param.getCatalog3Id() != null) {
            boolBuilder.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolBuilder.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        boolBuilder.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));

        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] split = param.getSkuPrice().split("_");
            if (split.length == 2) {
                rangeQuery.gte(split[0]).lte(split[1]);
            } else if (split.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(split[0]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(split[0]);
                }
            }
            boolBuilder.filter(rangeQuery);


        }

        if (param.getAttrs() != null && param.getAttrs().size() > 0) {

            for (String attr : param.getAttrs()) {
                //attrs=1_6.5英寸:7.5英寸
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] split = attr.split("_");
                String attrId = split[0];
                String[] attrValues = split[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolBuilder.filter(nestedQuery);
            }

        }

        sourceBuilder.query(boolBuilder);
        //排序、分页、高亮
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();      // sort=saleCount_desc/asc
            String[] split = sort.split("_");
            SortOrder sortOrder = split[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(split[0], sortOrder);
        }
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_DEFAULT_PAGE_SIZE);
        sourceBuilder.size(EsConstant.PRODUCT_DEFAULT_PAGE_SIZE);

        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        //聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(50);
        //子聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName.keyword")).size(1);
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg.keyword")).size(1);
        sourceBuilder.aggregation(brandAgg);
        //分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(30);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName.keyword")).size(1);
        sourceBuilder.aggregation(catalog_agg);
        //属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        TermsAggregationBuilder attrNameAgg = AggregationBuilders.terms("attr_Name_agg").field("attrs.attrName.keyword").size(1);
        TermsAggregationBuilder attrValueAgg = AggregationBuilders.terms("attr_Value_agg").field("attrs.attrValue.keyword").size(50);
        attr_agg.subAggregation(attrIdAgg);
        attrIdAgg.subAggregation(attrNameAgg);
        attrIdAgg.subAggregation(attrValueAgg);
        sourceBuilder.aggregation(attr_agg);


        String dsl = sourceBuilder.toString();
        System.out.println("DSL语句生成:" + dsl);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);


        return searchRequest;
    }


}
