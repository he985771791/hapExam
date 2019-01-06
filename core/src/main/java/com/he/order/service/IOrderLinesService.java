package com.he.order.service;

import com.hand.hap.core.IRequest;
import com.hand.hap.core.ProxySelf;
import com.hand.hap.system.service.IBaseService;
import com.he.order.dto.OrderLines;

import java.util.List;

public interface IOrderLinesService extends IBaseService<OrderLines>, ProxySelf<IOrderLinesService>{
    List<OrderLines> orderDetails(IRequest request, OrderLines orderLine, int pageNum, int pageSize);
}