package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.bean.enums.PaymentStatus;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.gmallusermanage.service.OrderService;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Reference
    OrderService orderService;

    @Autowired
    PaymentInfoService paymentInfoService;

    @Autowired
    AlipayClient alipayClient;

    @RequestMapping("/index")
    @LoginRequire
    public String index(HttpServletRequest request){
        String userId = (String)request.getAttribute("userId");
        String orderId = (String) request.getAttribute("orderId");

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        request.setAttribute("orderId",orderId);

        return  "paymentindex";

    }

    @RequestMapping(value = "/alipay/submit",method = RequestMethod.POST)
    public String submitPayment(HttpServletRequest request, HttpServletResponse response){

        String orderId = (String)request.getAttribute("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        PaymentInfo paymentInfo=new PaymentInfo();

        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderId);
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);

        paymentInfoService.savePaymentInfo(paymentInfo);
        //4 制作支付宝参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);

        Map<String,Object> bizContnetMap=new HashMap<>();
        bizContnetMap.put("out_trade_no",paymentInfo.getOutTradeNo());
        bizContnetMap.put("subject",paymentInfo.getSubject());
        bizContnetMap.put("total_amount",paymentInfo.getTotalAmount());
        bizContnetMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        String bizContent = JSON.toJSONString(bizContnetMap);
        alipayRequest.setBizContent(bizContent);
        String form=null;
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8" );
        return form;

    }
    @RequestMapping("/alipay/callback/return")
    public String paymentReturn(){
        return "redirect://"+AlipayConfig.return_order_url;
    }
    /***
     * 接收异步通知
     * 1、 验证签名
     * 2、 判断成功标志
     * 3、 该单据是否已经处理
     * 4、 修改支付信息状态
     * 5、 通知订单模块
     * 6、 给支付宝回执 success
     */

    @RequestMapping(value = "/alipay/callback/notify",method = RequestMethod.POST)
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String,String> paramMap, HttpServletRequest request){
        String sign = request.getParameter("sign");
        boolean isChecked=false;
        try {
            isChecked = AlipaySignature.rsaCheckV2(paramMap, AlipayConfig.alipay_public_key, "UTF-8");
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(!isChecked){
            return "fail";
        }

        String trade_status = paramMap.get("trade_status");
        if("TRADE_SUCCESS".equals(trade_status)||"TRADE_FINISHED".equals(trade_status)){

            String outTradeNo = paramMap.get("out_trade_no");

            PaymentInfo paymentInfoQuery=new PaymentInfo();
            paymentInfoQuery.setOutTradeNo(outTradeNo);

            PaymentInfo paymentInfo = paymentInfoService.getPaymentInfo(paymentInfoQuery);

            if(paymentInfo.getPaymentStatus()==PaymentStatus.PAID||paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED){
                return "fail";
            }else{
                //修改状态
                PaymentInfo paymentInfoForUpdate=new PaymentInfo();
                paymentInfoForUpdate.setPaymentStatus(PaymentStatus.PAID);
                paymentInfoForUpdate.setCallbackTime(new Date());
                paymentInfoForUpdate.setCallbackContent(paramMap.toString());
                paymentInfoService.updatePaymentInfo(paymentInfo.getOutTradeNo(),paymentInfoForUpdate);
                //发送通知给订单 // 消息队列
                paymentInfoService.sendPaymentResult(paymentInfo,"success");
                System.out.println(" 订单编号【 " +paymentInfo.getOrderId()+"】支付状态更新成功！！" );
                return "success";

            }

        }

        return "fail";
    }

    @RequestMapping("sendPaymentResult")
   // @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,@RequestParam("result")String result){
        paymentInfoService.sendPaymentResult(paymentInfo,result);
        return "sent payment result";

    }
}
