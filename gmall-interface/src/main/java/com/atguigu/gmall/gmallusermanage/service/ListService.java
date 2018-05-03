package com.atguigu.gmall.gmallusermanage.service;

import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParam;
import com.atguigu.gmall.bean.SkuLsResult;

import javax.naming.directory.SearchResult;


public interface ListService{

    public void saveSkuInfo(SkuLsInfo skuLsInfo);

    public SkuLsResult searchSkuinfoList(SkuLsParam skuLsParams);


    public String makeQueryStringForSearch(SkuLsParam skuLsParam);

    public void incrHotScore(String skuId);

}
