package com.he.order.mapper;

import com.hand.hap.mybatis.common.Mapper;
import com.he.order.dto.Customers;

import java.util.List;

public interface CustomersMapper extends Mapper<Customers>{
    List<Customers> lovCustomers(Customers customer);
    List<Customers> lovCustomersForOrderLines(Customers customer);
}