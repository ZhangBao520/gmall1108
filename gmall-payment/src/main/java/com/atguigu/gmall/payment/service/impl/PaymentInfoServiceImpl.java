package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentMapper;
import com.atguigu.gmall.payment.service.PaymentInfoService;
import com.atguigu.gmall.mq.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

@Service
public class PaymentInfoServiceImpl implements PaymentInfoService{

    @Autowired
    PaymentMapper paymentMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    public  void savePaymentInfo(PaymentInfo paymentInfo){

        paymentMapper.insertSelective(paymentInfo);

    }

    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery){
        PaymentInfo paymentInfo= paymentMapper.selectOne(paymentInfoQuery);
        return  paymentInfo;
    }


    public void updatePaymentInfo(String outTradeNo,PaymentInfo paymentInfo){
        Example example=new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",outTradeNo);
        paymentMapper.updateByExampleSelective(paymentInfo,example);
        return   ;
    }
    public void sendPaymentResult(PaymentInfo paymentInfo,String result){
        //发送支付结果
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(paymentResultQueue);
            MapMessage mapMessage= new ActiveMQMapMessage();
            mapMessage.setString("orderId",paymentInfo.getOrderId());
            mapMessage.setString("result",result);
            producer.send(mapMessage);

            session.commit();
            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}
