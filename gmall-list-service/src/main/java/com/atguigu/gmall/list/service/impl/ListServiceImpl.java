package com.atguigu.gmall.list.service.impl;


import ch.qos.logback.core.net.SyslogOutputStream;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParam;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.gmallusermanage.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

    @Autowired
    RedisUtil redisUtil;

 public static final String index_name_gmall = "gmall";

 public static final String type_name_gmall ="SkuInfo";

    public void saveSkuInfo(SkuLsInfo skuLsInfo){
        Index index= new Index.Builder(skuLsInfo).index(index_name_gmall).type(type_name_gmall).id(skuLsInfo.getId()).build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String makeQueryStringForSearch(SkuLsParam skuLsParam){
        SearchSourceBuilder searchSourceBuilder =new SearchSourceBuilder();

        //复合查询
        BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();

        if(skuLsParam.getKeyword()!=null){
            //关键词
            MatchQueryBuilder queryBuilder=new MatchQueryBuilder("skuName",skuLsParam.getKeyword());
            boolQueryBuilder.must(queryBuilder);

            //高亮
            HighlightBuilder highlightBuilder=new HighlightBuilder();
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");
            highlightBuilder.field("skuName");
            searchSourceBuilder.highlight(highlightBuilder);
        }

        if(skuLsParam.getCatalog3Id()!=null){
            //三级分类过滤
            TermQueryBuilder termQueryBuilder=new TermQueryBuilder("catalog3Id",skuLsParam.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }


        if(skuLsParam.getValueId()!=null&&skuLsParam.getValueId().length>0){
            //平台属性过滤
            for (int i = 0; i < skuLsParam.getValueId().length; i++) {
                String valueId= skuLsParam.getValueId()[i];
                TermQueryBuilder termQueryBuilder=new TermQueryBuilder("skuAttrValueList.valueId",valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }

        }

        searchSourceBuilder.query(boolQueryBuilder);


        //分页
        int from=(skuLsParam.getPageNo()-1)*skuLsParam.getPageSize();

        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParam.getPageSize());

        //排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        //聚合
        TermsBuilder groupby_valueId = AggregationBuilders.terms("groupby_valueId").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_valueId);

        System.out.println("searchSourceBuilder.toString() = " + searchSourceBuilder.toString());
        return searchSourceBuilder.toString();
    }


    public SkuLsResult makeResultForSearch(SkuLsParam skuLsParam, SearchResult searchResult){
        SkuLsResult skuLsResult = new SkuLsResult();
        System.err.println("skuLsResult"+skuLsResult);
        List<SkuLsInfo> list = new ArrayList<>(skuLsParam.getPageSize());
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;
            if (hit.highlight != null && hit.highlight.size() > 0) {
                List<String> list1 = hit.highlight.get("skuName");
                String s = list1.get(0);
                skuLsInfo.setSkuName(s);

            }
            System.out.println(skuLsInfo);
            list.add(skuLsInfo);
        }
        skuLsResult.setSkuLsInfoList(list);
        skuLsResult.setTotal(searchResult.getTotal());

        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation termsAggregation = aggregations.getTermsAggregation("groupby_valueId");
        List<TermsAggregation.Entry> buckets = termsAggregation.getBuckets();
        List keyList = new ArrayList(buckets.size());

        for (TermsAggregation.Entry bucket : buckets) {
            String key = bucket.getKey();
            keyList.add(key);
        }

        skuLsResult.setAttrValueIdList(keyList);

        System.out.println("keyList = " + keyList);
        return skuLsResult;
    }


    public SkuLsResult searchSkuinfoList(SkuLsParam skuLsParams){
        String query = makeQueryStringForSearch(skuLsParams);
        Search search = new Search.Builder(query).addIndex(index_name_gmall).addType(type_name_gmall).build();
        SearchResult searchResult=null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SkuLsResult skuLsResult = makeResultForSearch(skuLsParams, searchResult);
        System.err.println("skuLs---------------------------"+skuLsResult);
        return skuLsResult;
    }
    public void incrHotScore(String skuId){

        Jedis jedis = redisUtil.getJedis();

        Double hotScore = jedis.zincrby("hotScore", 1, skuId);

        if(hotScore%10==0){
            updateHotScore(  skuId ,  hotScore);
        }

    }


    public void updateHotScore(String skuId ,Double hotScore){
        String updateJson="{\n" +
                "  \"doc\": {\n" +
                "    \"hotScore\":\""+hotScore+"\"\n" +
                "  }\n" +
                "}";


        Update update = new Update.Builder(updateJson).index(index_name_gmall).type(type_name_gmall).id(skuId).build();

        try {
            DocumentResult result = jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ;
    }

}
