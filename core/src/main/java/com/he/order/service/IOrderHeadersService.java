package com.he.order.service;

import com.hand.hap.core.IRequest;
import com.hand.hap.core.ProxySelf;
import com.hand.hap.system.service.IBaseService;
import com.hand.hap.task.exception.TaskExecuteException;
import com.he.order.dto.OrderHeaders;
import com.he.order.mapper.OrderHeadersMapper;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IOrderHeadersService extends IBaseService<OrderHeaders>, ProxySelf<IOrderHeadersService>{
    /*查询订单头信息*/
    /*如果其他bean调用这个方法,在其他bean中声明事务,那就用事务.如果其他bean没有声明事务,那就不用事务*/
    @Transactional(propagation = Propagation.SUPPORTS)
    List<OrderHeaders> selectCollect(IRequest request, OrderHeaders orderHeader,int pageNum,int pageSize);
    /*保存订单头行*/
    @Transactional(propagation = Propagation.SUPPORTS)
    List<OrderHeaders> saveHeaderAndLines(IRequest request,List<OrderHeaders> dto,int pageNum,int pageSize) throws TaskExecuteException;
    /*整单删除*/
    int deleteHeaderAndLines(IRequest request,List<OrderHeaders> orderHeaders);
    /*excel导出*/
    SXSSFWorkbook buildExportOrderExcel(IRequest requestContext, OrderHeaders dto, int page, int pageSize);
}