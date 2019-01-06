package com.he.order.service.impl;

import com.hand.hap.system.service.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;
import com.he.order.dto.Customers;
import com.he.order.service.ICustomersService;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomersServiceImpl extends BaseServiceImpl<Customers> implements ICustomersService{

}