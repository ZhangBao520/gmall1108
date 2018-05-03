package com.atguigu.gmall.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.gmallusermanage.service.ListService;
import com.atguigu.gmall.gmallusermanage.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    ListService listService;

    @Reference
    ManageService manageService;

    @RequestMapping("list.html")
    public String list(SkuLsParam skuLsParam, HttpServletRequest httpServletRequest){

        SkuLsResult skuLsResult = listService.searchSkuinfoList(skuLsParam);

        List<String> valueIdList = skuLsResult.getAttrValueIdList();

        List<BaseAttrInfo> attrList = manageService.getAttrList(valueIdList);
        List<BaseAttrValue> selectedValueList=new ArrayList<>(valueIdList.size());

        String urlParam=makeUrlParam(skuLsParam);

        for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo =  iterator.next();
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {


                baseAttrValue.setUrlParam(urlParam);
                if(skuLsParam.getValueId()!=null&&skuLsParam.getValueId().length>0){
                    for (String valueId : skuLsParam.getValueId()) {
                        if(valueId.equals(baseAttrValue.getId())){
                            iterator.remove();

                            //构造面包屑
                            BaseAttrValue  attrValueSelected=new BaseAttrValue();
                            attrValueSelected.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
                            attrValueSelected.setId(valueId);
                            attrValueSelected.setUrlParam(makeUrlParam(skuLsParam,valueId));
                            selectedValueList.add(attrValueSelected);
                        }

                    }
                }

            }

        }

        int totalPages = (int) ((skuLsResult.getTotal() + skuLsParam.getPageSize() - 1) / skuLsParam.getPageSize());

        httpServletRequest.setAttribute("totalPages",totalPages);

        httpServletRequest.setAttribute("pageNo",skuLsParam.getPageNo());

        httpServletRequest.setAttribute("urlParam",urlParam);

        httpServletRequest.setAttribute("keyword",skuLsParam.getKeyword());

        httpServletRequest.setAttribute("selectedValueList",selectedValueList);

        httpServletRequest.setAttribute("attrList",attrList);

        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();

        httpServletRequest.setAttribute("skuLsInfoList",skuLsInfoList);
        return "list";
    }
    private  String makeUrlParam(SkuLsParam skuLsParam,String... excludeValueIds){
        String urlParam="";
        if(skuLsParam.getKeyword()!=null){
            urlParam+="keyword="+skuLsParam.getKeyword();
        }
        if(skuLsParam.getCatalog3Id()!=null){
            if(urlParam.length()>0){
                urlParam+="&";
            }
            urlParam+="catalog3Id="+skuLsParam.getCatalog3Id();
        }
        if(skuLsParam.getValueId()!=null&&skuLsParam.getValueId().length>0){

            for (int i = 0; i < skuLsParam.getValueId().length; i++) {
                String valueId  = skuLsParam.getValueId()[i];
                //排除选中的属性值
                if(excludeValueIds!=null&&excludeValueIds.length>0){
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)){
                        continue;
                    }
                }

                if(urlParam.length()>0){
                    urlParam+="&";
                }
                urlParam+="valueId="+valueId;
            }
        }
        return  urlParam;

    }

}
