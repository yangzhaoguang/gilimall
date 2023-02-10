package com.atguigu.gulimall.seartch.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seartch.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.seartch.constant.EsConstant;
import com.atguigu.gulimall.seartch.feign.ProductFeignService;
import com.atguigu.gulimall.seartch.service.MallSearchService;
import com.atguigu.gulimall.seartch.vo.AttrResponseVo;
import com.atguigu.gulimall.seartch.vo.BrandResponseVo;
import com.atguigu.gulimall.seartch.vo.SearchParamVo;
import com.atguigu.gulimall.seartch.vo.SearchResultVo;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * Author: YZG
 * Date: 2023/1/21 22:59
 * Description: 
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
   private  ProductFeignService productFeignService;

    /*
     * 检索服务
     * */
    @Override
    public SearchResultVo search(SearchParamVo searchParam) {
        // 构建查询请求
        SearchRequest searchRequest = builderSearchRequest(searchParam);

        SearchResultVo result = null;
        try {
            // 发送请求
            SearchResponse response = restHighLevelClient.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
            // 封装返回结果
            result = builderSearchResult(response, searchParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /*
     * 构建查询请求
     *  模糊匹配，品牌查询、分类查询、库存查询、价格区间查询、排序、分页查询、高亮显示、聚合
     * */
    private SearchRequest builderSearchRequest(SearchParamVo searchParam) {
        SearchSourceBuilder builder = new SearchSourceBuilder();

        /**
         * 查询
         */
        // 1、模糊匹配
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
        // 2、过滤
        // 2.1 分类
        if (searchParam.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }
        // 2.2 品牌
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }
        // 2.3 属性
        // 属性参数格式: attr=1_value:value，2_value:value
        List<String> attrs = searchParam.getAttrs();
        if (attrs != null && attrs.size() > 0) {
            for (String attr : attrs) {
                // 嵌入式
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrsValue = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrsValue));
                // 每一个属性条件都应有一个 filter，不应该将所有的属性条件都放在一个filter里面。
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        // 2.4 库存
        if (searchParam.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() != 0));
        }
        // 2.5 价格区间
        /**
         * 价格区间的参数格式： 1_500,_500,500_
         *           "range": {
         *             "skuPrice": {
         *               "gte": "",
         *               "lte": "",
         *             }
         *           }
         */
        String price = searchParam.getSkuPrice();
        if (!StringUtils.isEmpty(price)) {
            // 根据_分割
            String[] s = price.split("_");
            if (s.length == 2) {
                if (price.startsWith("_")) {
                    // _500
                    boolQuery.filter(QueryBuilders.rangeQuery("skuPrice").lte(s[1]));
                } else {
                    // 1_500
                    boolQuery.filter(QueryBuilders.rangeQuery("skuPrice").gte(s[0]).lte(s[1]));
                }
            } else {
                // 500_
                boolQuery.filter(QueryBuilders.rangeQuery("skuPrice").gte(s[0]));
            }
        }
        // 将以上条件封装
        builder.query(boolQuery);


        /**
         * 排序
         *  排序的格式：
         *     saleCount(销量)、hotScore(热度)、skuPrice(价格)
         *     sort=saleCount_asc/desc
         *     sort=hotScore_asc/desc
         *     sort=skuPrice_asc/desc
         * 分页
         *   计算方式:
         *      from: 从第几条数据开始显示
         *      size：每页显示条数
         *      pageNum：当前页码
         *      from = (pageNum -1) * size
         * 高亮
         *  只有进行模糊匹配时，对标题进行高亮
         */
        // 3、排序
        String sort = searchParam.getSort();
        if (!StringUtils.isEmpty(sort)) {
            String[] strings = sort.split("_");
            String filed = strings[0];
            String order = strings[1];
            builder.sort(filed, "asc".equalsIgnoreCase(order) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 4、分页
        builder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE);
        builder.size(EsConstant.PRODUCT_PAGE_SIZE);

        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            // 5、高亮
            builder.highlighter(new HighlightBuilder().field("skuTitle").preTags("<b style='color:red'>").postTags("</b>"));
        }

        /**
         * 聚合分析
         * */

        // 1、品牌聚合
        //subAggregation：子聚合
        TermsAggregationBuilder brandAgg =
                AggregationBuilders.terms("brand_agg").field("brandId").size(50)
                        .subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1))
                        .subAggregation(AggregationBuilders.terms("brand_image_agg").field("brandImg").size(1));


        // 2、分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20)
                .subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));


        // 3、属性聚合
        NestedAggregationBuilder nestedAgg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(20);
        // attrId 对应的 AttrName 、AttrValue
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1))
                .subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(10));
        nestedAgg.subAggregation(attrIdAgg);

        builder.aggregation(brandAgg);
        builder.aggregation(catalogAgg);
        builder.aggregation(nestedAgg);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, builder);
        System.out.println("DSL语句: " + builder.toString());
        return searchRequest;
    }

    /*
     * 封装返回结果
     * */
    private SearchResultVo builderSearchResult(SearchResponse response, SearchParamVo searchParam) {
        SearchResultVo resultVo = new SearchResultVo();
        // // 1、商品信息
        SearchHit[] hits = response.getHits().getHits();
        if (hits.length > 0 && hits != null) {
            List<SkuEsModel> products = Arrays.stream(hits).map(item -> {
                String product = item.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(product, SkuEsModel.class);
                if (!StringUtils.isEmpty(searchParam.getKeyword())) {
                    // 设置标题高亮
                    String skuTitle = item.getHighlightFields().get("skuTitle").fragments()[0].string();
                    skuEsModel.setSkuTitle(skuTitle);
                }

                return skuEsModel;
            }).collect(Collectors.toList());
            resultVo.setProducts(products);
        }

        // // 2、品牌信息

        // 2.1 获取品牌的聚合信息
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        List<SearchResultVo.BrandVo> brandVos = brandAgg.getBuckets().stream().map(bucket -> {
            // 品牌Id
            String brandId = bucket.getKeyAsString();
            SearchResultVo.BrandVo brandVo = new SearchResultVo.BrandVo();
            // 2.2 获取子聚合品牌图片信息
            String img =
                    ((ParsedStringTerms) bucket.getAggregations().get("brand_image_agg")).getBuckets().get(0).getKeyAsString();

            // 2.3 获取子聚合品牌名信息
            String BrandName =
                    ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();

            brandVo.setBrandId(Long.parseLong(brandId));
            brandVo.setBrandImg(img);
            brandVo.setBrandName(BrandName);
            return brandVo;
        }).collect(Collectors.toList());
        resultVo.setBrands(brandVos);
        // // 3、分类信息
        // 3、1 获取分类聚合信息
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalog_agg");
        List<SearchResultVo.CatalogVo> catalogVos = catalogAgg.getBuckets().stream().map(bucket -> {
            SearchResultVo.CatalogVo catalogVo = new SearchResultVo.CatalogVo();
            // 分类ID
            String catalogId = bucket.getKeyAsString();
            // 3、2 获取子聚合 分类名信息
            String catalogName =
                    ((ParsedStringTerms) bucket.getAggregations().get("catalog_name_agg")).getBuckets().get(0).getKeyAsString();

            catalogVo.setCatalogId(Long.parseLong(catalogId));
            catalogVo.setCatalogName(catalogName);
            return catalogVo;
        }).collect(Collectors.toList());
        resultVo.setCatalogs(catalogVos);

        // // 4、属性信息
        // 4、1 获取属性聚合信息
        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        // 4、2 获取子聚合 attrIdAgg
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");

        List<SearchResultVo.AttrsVo> attrsVos = attrIdAgg.getBuckets().stream().map(bucket -> {
            SearchResultVo.AttrsVo attrsVo = new SearchResultVo.AttrsVo();
            String attrId = bucket.getKeyAsString();
            // 4、3 获取子聚合属性名信息
            String attrName =
                    ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            // 4、3 获取子聚合属性值信息.属性值可能有多个
            List<String> attrValues
                    = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets()
                    .stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());


            attrsVo.setAttrId(Long.parseLong(attrId));
            attrsVo.setAttrName(attrName);
            attrsVo.setAttrValue(attrValues);

            return attrsVo;
        }).collect(Collectors.toList());
        resultVo.setAttrs(attrsVos);

        // 5、分页信息：当前页、总记录数、总页码数
        resultVo.setPageNum(searchParam.getPageNum());
        resultVo.setTotal(response.getHits().getTotalHits().value);
        // 页码数 = 总记录数 / 每页记录数 .... 如果有余数 + 1
        Integer pageSize = EsConstant.PRODUCT_PAGE_SIZE;
        Long total = resultVo.getTotal();
        resultVo.setTotalPages((int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1));

        // 页码集合
        ArrayList<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= resultVo.getTotalPages(); i++) {
            pageNavs.add(i);
        }
        resultVo.setPageNavs(pageNavs);

        /*
         * 属性 面包屑导航
         * */

        // 保存面包屑导航
        List<SearchResultVo.NavVo> navVos = resultVo.getNavs();
        // 当URL中有属性条件时才会封装面包屑
        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0 ) {

            List<String> attrs = searchParam.getAttrs();
            for (String attr : attrs) {
                SearchResultVo.NavVo navVo = new SearchResultVo.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                try {
                    // 远程调用，查询属性信息
                    R r = productFeignService.getAttrById(Long.parseLong(s[0]));
                    // 设置筛选属性的id
                    resultVo.getAttrIds().add(Long.parseLong(s[0]));
                    AttrResponseVo attrResponseVo = r.getData("attr", new TypeReference<AttrResponseVo>() {});
                    navVo.setNavName(attrResponseVo.getAttrName());

                    String link = replaceQueryString(searchParam, attr,"attrs");
                    navVo.setNavLink("http://search.gulimall.com/list.html?" + link);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                navVos.add(navVo);
            }
            resultVo.setNavs(navVos);
        }

        /*
        * 品牌面包屑
        * */
        List<SearchResultVo.NavVo> navs = resultVo.getNavs();

        List<Long> brandIds = searchParam.getBrandId();
        if (brandIds != null && brandIds.size() > 0) {
            SearchResultVo.NavVo navVo = new SearchResultVo.NavVo();
            navVo.setNavName("品牌");
            R r = productFeignService.getBrandsByIds(brandIds);
            if (r.getCode() == 0) {
                List<BrandResponseVo> brands = r.getData("brands", new TypeReference<List<BrandResponseVo>>() {});
                // 品牌可能有多个，将品牌名拼接起来
                StringBuffer stringBuffer = new StringBuffer();
                String link = "";
                for (BrandResponseVo brand : brands) {
                   stringBuffer.append(brand.getName() + " ");
                    link = replaceQueryString(searchParam, brand.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(stringBuffer.toString());
                navVo.setNavLink("http://search.gulimall.com/list.html?" + link);
                navs.add(navVo);

                resultVo.setNavs(navs);
            }
        }


        // navVo.setNavValue();
        // navVo.setNavLink();

        return resultVo;
    }

    /*
    * 对指定key进行替换并进行转码
    * */
    @NotNull
    private String replaceQueryString(SearchParamVo searchParam, String attr,String key)  {
        // 替换URL中的属性参数：
        // 原始: http://search.gulimall.com/list.html?catalog3Id=225&attrs=2_MGA-AL00
        // 替换完: http://search.gulimall.com/list.html?catalog3Id=225
        // URL 参数需要进行编码
        String encode = null;
        try {
            encode = URLEncoder.encode(attr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        encode = attr.replace("+","%20"); // Java中对空格编码会转化成+，而实际上前端的空格表示 %20
        String link = searchParam.get_queryString().replace("&"+key+"=" + encode, "");
        return link;
    }


}
