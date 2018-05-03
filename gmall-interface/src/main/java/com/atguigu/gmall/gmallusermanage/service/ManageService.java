package com.atguigu.gmall.gmallusermanage.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

public interface ManageService {

    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    public List<BaseAttrInfo> getAttrList(String catalog2Id);

    public BaseAttrInfo getAttrInfo(String id);

    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    public  List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    public List<BaseSaleAttr> getBaseSaleAttrList();

    public void saveSpuInfo(SpuInfo spuInfo);

    public List<SpuImage> getSpuImageList(String spuId);

    public  List<SpuSaleAttrValue> getSpuSaleAttrValueList(SpuSaleAttrValue spuSaleAttrValue);

    public List<BaseAttrInfo> getAttrList3(String catalog3Id);

    public void saveSkuInfo(SkuInfo skuInfo);

    public List<SkuInfo> getSkuInfoListBySpu(String spuId);

    public SkuInfo getSkuInfo(String skuId);

    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);

    public SkuInfo getSkuInfoFromDB(String skuId);
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}

