package com.wei.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.wei.gulimall.search.config.EsConfig;
import jdk.jfr.DataAmount;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minidev.json.JSONArray;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class GulimallSearchApplicationTests {
    @Autowired
    private RestHighLevelClient client;

    @Test
    public void contextLoads() {
        System.out.println(client);
    }
    @Data
    @AllArgsConstructor
    class User{
        private String userName;
        private String gender;
        private Integer age;
    }
    @Test
    void testSearchApi() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");      //指定索引
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));

        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(aggregationBuilder);

        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(balanceAvg);

        System.out.println(searchSourceBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, EsConfig.COMMON_OPTIONS);
        System.out.println(searchResponse.toString());

        //获取命中
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            System.out.println(searchHit.toString());
        }
    }
    @Test
    void indexData() throws IOException {
        IndexRequest request = new IndexRequest("user");
        request.id("1");
        User user = new User("无敌鸿伟","男",21);
        String toJSONString = JSON.toJSONString(user);
        request.source(toJSONString, XContentType.JSON);
        IndexResponse index = client.index(request, EsConfig.COMMON_OPTIONS);
        System.out.println(index);
    }


}
