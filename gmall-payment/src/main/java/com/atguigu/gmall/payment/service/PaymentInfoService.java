package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface PaymentInfoService {

    public  void savePaymentInfo(PaymentInfo paymentInfo);
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    public void updatePaymentInfo(String outTradeNo,PaymentInfo paymentInfo);

    public void sendPaymentResult(PaymentInfo paymentInfo,String result);

    public boolean  checkPayment(PaymentInfo paymentInfoQuery);

    public void closePayment(String orderId);

    public void sendDelayPaymentResultCheck(String outTradeNo,int delaySec,int checkCount);

}
