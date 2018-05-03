package com.atguigu.gmall.gmallusermanage.service;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.ProcessStatus;

import java.util.List;

public interface OrderService {

    public  void saveOrder(OrderInfo orderInfo);

    public String genTradeCode(String userId);

    public boolean checkTradeCode(String userId,String tradeCodePage);

    public void delTradeCode(String userId);

    public List<OrderInfo> getOrderListByUser(String userId);

    public OrderInfo getOrderInfo(String orderId);

    public void updateOrderStatus(String orderId, ProcessStatus processStatus);

    public void sendOrderStatus(String orderId);


}
