package com.atguigu.gmall.order.mapper;

import com.atguigu.gmall.bean.OrderInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @param
 * @return
 */
public interface OrderInfoMapper extends Mapper<OrderInfo> {
    public List<OrderInfo> selectOrderListByUser(Long userId);
}
