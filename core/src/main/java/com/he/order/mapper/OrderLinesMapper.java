package com.he.order.mapper;

import com.hand.hap.mybatis.common.Mapper;
import com.he.order.dto.Companys;
import com.he.order.dto.OrderLines;

import java.util.List;

public interface OrderLinesMapper extends Mapper<OrderLines>{
    List<OrderLines> orderDetails(OrderLines orderLine);
}