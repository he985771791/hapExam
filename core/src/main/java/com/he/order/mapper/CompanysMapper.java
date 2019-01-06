package com.he.order.mapper;

import com.hand.hap.mybatis.common.Mapper;
import com.he.order.dto.Companys;

import java.util.List;

public interface CompanysMapper extends Mapper<Companys>{
    List<Companys> lovCompanys(Companys company);
    List<Companys> lovCompanysForOrderLines(Companys company);
}