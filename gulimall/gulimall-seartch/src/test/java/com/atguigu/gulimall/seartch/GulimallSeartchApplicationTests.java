package com.atguigu.gulimall.seartch;


import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.seartch.config.GulimallElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import net.minidev.json.JSONValue;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSeartchApplicationTests {


    @Data
    @ToString
    static class AccountBean {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /*
     * 案例
     * */
    @Test
    public void caseTwo() throws IOException {
        // 搜索 address 中包含 mill 的所有人的年龄分布以及平均年龄，但不显示这些人的详情。
        SearchRequest searchRequest = new SearchRequest("bank");
        // 构建DSL 查询条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        sourceBuilder.size(0);
        // 构建聚合
        TermsAggregationBuilder aggregation1 = AggregationBuilders.terms("ageAgg").field("age");
        AvgAggregationBuilder aggregation2 = AggregationBuilders.avg("avgAgg").field("age");

        sourceBuilder.aggregation(aggregation1);
        sourceBuilder.aggregation(aggregation2);
        searchRequest.source(sourceBuilder);

        System.out.println("条件: " + sourceBuilder);
        // 发送请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        // 获取聚合结果
        Terms ageAgg = searchResponse.getAggregations().get("ageAgg");
        for (Terms.Bucket bucket : ageAgg.getBuckets()) {
            System.out.println("年龄： " + bucket.getKey());
            System.out.println("人数： " + bucket.getDocCount());
        }

        Avg avgAgg = searchResponse.getAggregations().get("avgAgg");
        System.out.println("平均年龄: " + avgAgg.getValue());
    }


    @Test
    public void search_1() throws IOException {
        SearchRequest searchRequest = new SearchRequest("bank");
        // 1、sourceBuilder 构建DSL检索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // query中的match
        // sourceBuilder.query(QueryBuilders.matchQuery("address","mill"));

        // query 中的 match_phrase
        // sourceBuilder.query(QueryBuilders.matchPhraseQuery("address","Holmes Lane "));

        // 2、sourceBuilder 同样可以构建分页信息
        // sourceBuilder.from(10);
        // sourceBuilder.size(20);

        // query中的match_all
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(sourceBuilder);
        // 执行检索
        SearchResponse response = restHighLevelClient.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println("响应结果: " + response);
    }

    /*
     * 复杂查询
     * */
    @Test
    public void search() throws IOException {
        // 案例: 按照年龄聚合，并且请求这些年龄段的这些人的平均薪资
        SearchRequest searchRequest = new SearchRequest("bank");
        // 1、sourceBuilder 构建DSL检索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // query中的match
        // sourceBuilder.query(QueryBuilders.matchQuery("address","mill"));

        // query 中的 match_phrase
        // sourceBuilder.query(QueryBuilders.matchPhraseQuery("address","Holmes Lane "));

        // 2、sourceBuilder 同样可以构建分页信息
        // sourceBuilder.from(10);
        // sourceBuilder.size(20);

        // query中的match_all
        sourceBuilder.query(QueryBuilders.matchAllQuery());


        // 3、sourceBuilder封装聚合条件
        // subAggregation 可嵌套聚合
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("ageAgg").field("age").size(10)
                .subAggregation(AggregationBuilders.avg("BalanceAgg").field("balance"));
        sourceBuilder.aggregation(aggregation);

        searchRequest.source(sourceBuilder);
        System.out.println("查询条件: " + sourceBuilder);

        // 执行检索
        SearchResponse response = restHighLevelClient.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println("响应结果: " + response);

        // 4、分析结果
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();

        for (SearchHit searchHit : searchHits) {
            // 查询结果
            String sourceAsString = searchHit.getSourceAsString();
            // 通过JSON转换工具，将json——》实体类
            AccountBean accountBean = JSON.parseObject(sourceAsString, AccountBean.class);
            System.out.println("Account对象: " + accountBean);
        }
        // 5、查看聚合信息
        Aggregations aggregations = response.getAggregations();
        // 获取指定的聚合
        Terms ageAgg = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg.getBuckets()) {
            System.out.println("年龄: " + bucket.getKeyAsString());
            System.out.println("人数: " + bucket.getDocCount());
            // 获取嵌套的聚合
            Avg balanceAgg = bucket.getAggregations().get("BalanceAgg");
            System.out.println("平均薪资: " + balanceAgg.getValue());
        }
    }

    /*
     * 删除
     * */

    @Test
    public void delIndex() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("users", "1");
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(deleteRequest);
    }

    /*
     * 更新索引
     * */
    @Test
    public void update() throws IOException {
        // index，id
        UpdateRequest updateRequest = new UpdateRequest("users", "1");
        User user = new User();
        user.setName("李四");
        String jsonString = JSON.toJSONString(user);
        updateRequest.doc(jsonString, XContentType.JSON);
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(updateResponse);
    }

    /**
     * 增加索引
     * */
    @Test
    public void addIndex() throws IOException {
        // 参数为索引名
        IndexRequest request = new IndexRequest("users");
        // 设置id
        // request.id("1");
        // 第一种封装数据方式
        // request.source("name","张三","age","17","address","河北");
        // 第二种封装数据方式
        User user = new User();
        user.setName("王五");
        user.setAge(22);
        user.setAddress("成华大道");
        // 将对象转换为 json
        String toJSONString = JSON.toJSONString(user);
        request.source(toJSONString, XContentType.JSON);
        // 发送请求新增索引
        IndexResponse response = restHighLevelClient.index(request, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(response);
    }


    @Data
    class User {
        private String name;
        private Integer age;
        private String address;
    }

    @Test
    public void contextLoads() {
        String s = "_200";
        String s1 = "1_200";
        String s2 = "200_";
        System.out.println(s.split("_").length); //2
        System.out.println(s1.split("_").length); // 2
        System.out.println(s2.split("_").length); //1


        String s4 = "12_MGA-AL00";
        String[] s5 = s4.split("_");
        String[] strings = s5[1].split(":");
        System.out.println(Arrays.toString(strings));

    }

    @Test
    public void Test2() {
        String s = "你 好";
        String encode = null;
        try {
            encode = URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(encode);

    }

}
