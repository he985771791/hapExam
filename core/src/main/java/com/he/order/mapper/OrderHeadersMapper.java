package com.he.order.mapper;

import com.hand.hap.mybatis.common.Mapper;
import com.he.order.dto.OrderHeaders;

import java.util.List;

public interface OrderHeadersMapper extends Mapper<OrderHeaders>{
    /*查询订单头信息*/
    List<OrderHeaders> selectCollect(OrderHeaders orderHeader);
    int deleteHeaderAndLines(OrderHeaders orderHeader);
}