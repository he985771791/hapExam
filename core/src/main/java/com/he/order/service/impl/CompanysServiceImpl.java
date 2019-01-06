package com.he.order.service.impl;

import com.hand.hap.system.service.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;
import com.he.order.dto.Companys;
import com.he.order.service.ICompanysService;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class CompanysServiceImpl extends BaseServiceImpl<Companys> implements ICompanysService{

}