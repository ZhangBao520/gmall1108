package com.atguigu.gmall.gmallorderweb.controller;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.bean.enums.OrderStatus;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.gmallusermanage.service.CartService;
import com.atguigu.gmall.gmallusermanage.service.ManageService;
import com.atguigu.gmall.gmallusermanage.service.OrderService;
import com.atguigu.gmall.gmallusermanage.service.UserInfoService;
import com.atguigu.gmall.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    UserInfoService userInfoService;

    @Reference
    CartService cartService;

    @Reference
    ManageService manageService;

    @Reference
    OrderService orderService;



    @RequestMapping("trade")
    @LoginRequire
    public String trade(HttpServletRequest httpServletRequest){
        String userId = (String) httpServletRequest.getAttribute("userId");
        List<UserAddress> userAddressList = userInfoService.getUserAddressList(userId);
        httpServletRequest.setAttribute("userAddressList",userAddressList);

        List<CartInfo> cartCheckedList = cartService.getCartCheckedList(userId);

        List<OrderDetail> orderDetailList=new ArrayList<>(cartCheckedList.size());
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail=new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetailList.add(orderDetail);
        }
        httpServletRequest.setAttribute("orderDetailList",orderDetailList);
        OrderInfo orderInfo=new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();

        httpServletRequest.setAttribute("totalAmount",orderInfo.getTotalAmount());

        String tradeCode = orderService.genTradeCode(userId);

        httpServletRequest.setAttribute("tradeCode",tradeCode);

        return "trade";
    }

    @RequestMapping("submitOrder")
    @LoginRequire
    public  String submitOrder(OrderInfo orderInfo, HttpServletRequest request){
        //0 检查tradeCode
        String userId =(String) request.getAttribute("userId");
        String tradeCode = request.getParameter("tradeCode");
        boolean existsTradeCode = orderService.checkTradeCode(userId, tradeCode);
        if(!existsTradeCode){
            request.setAttribute("errMsg","该页面已失效，请重新结算！");
            return "tradeFail";
        }

        //1 初始化参数

        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);
        //2 校验  验价
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            if(!skuInfo.getPrice().equals( orderDetail.getOrderPrice())){
                request.setAttribute("errMsg","您选择的商品可能存在价格变动，请重新下单。");
                cartService.loadCartCache(userId);
                return "tradeFail";
            }
            boolean hasStock = hasStorkBySkuidAndNum(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if(!hasStock){
                request.setAttribute("errMsg","您的商品【"+orderDetail.getSkuName()+"】库存不足，请重新下单。。");
                return "tradeFail";
            }

        }


        //3  保存
        orderService.saveOrder(orderInfo);
        orderService.delTradeCode(userId);
        //4 重定向
        return "redirect://payment.gmall.com/index";

    }
    @RequestMapping(value = "list",method = RequestMethod.GET)
    @LoginRequire
    public String getOrderList(HttpServletRequest httpServletRequest,Model model){
        String userId =(String) httpServletRequest.getAttribute("userId");
        List<OrderInfo> orderList  = orderService.getOrderListByUser(userId);
        model.addAttribute("orderList", orderList );
        return "list";
    }

    private boolean hasStorkBySkuidAndNum(String skuId,Integer skuNum) {
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        if("1".equals(result)){
            return true;
        }
        return false;
    }

}
