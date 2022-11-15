package com.wei.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.wei.common.to.es.EsSkuModel;
import com.wei.gulimall.search.config.EsConfig;
import com.wei.gulimall.search.constant.EsConstant;
import com.wei.gulimall.search.service.ProductUpService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServiceImpl implements ProductUpService {
    @Autowired
    RestHighLevelClient client;
    @Override
    public boolean StatusUp(List<EsSkuModel> esSkuModels) throws IOException {
        System.out.println("model列表:"+esSkuModels.toString());
        BulkRequest bulkRequest =new BulkRequest();
        for (EsSkuModel model : esSkuModels) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(model.getSkuId().toString());
            String jsonString = JSON.toJSONString(model);
            indexRequest.source(jsonString, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = client.bulk(bulkRequest, EsConfig.COMMON_OPTIONS);
        boolean hasFailures = bulk.hasFailures();
        System.out.println("桶处理是否出错？:"+hasFailures);
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> item.getId()).collect(Collectors.toList());
        System.out.println("商品上架:"+collect);
        return !hasFailures;
    }
}
