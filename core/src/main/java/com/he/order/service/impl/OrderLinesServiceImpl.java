package com.he.order.service.impl;

import com.github.pagehelper.PageHelper;
import com.hand.hap.core.IRequest;
import com.hand.hap.system.service.impl.BaseServiceImpl;
import com.he.order.mapper.OrderLinesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.he.order.dto.OrderLines;
import com.he.order.service.IOrderLinesService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrderLinesServiceImpl extends BaseServiceImpl<OrderLines> implements IOrderLinesService{
    @Autowired
    OrderLinesMapper orderLinesMapper;
    @Override
    public List<OrderLines> orderDetails(IRequest request, OrderLines orderLine, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        return orderLinesMapper.orderDetails(orderLine);
    }
}