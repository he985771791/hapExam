package com.he.order.service.impl;

import com.github.pagehelper.PageHelper;
import com.hand.hap.code.rule.exception.CodeRuleException;
import com.hand.hap.code.rule.service.ISysCodeRuleProcessService;
import com.hand.hap.core.IRequest;
import com.hand.hap.system.service.impl.BaseServiceImpl;
import com.hand.hap.task.exception.TaskExecuteException;
import com.he.order.dto.OrderLines;
import com.he.order.mapper.OrderHeadersMapper;
import com.he.order.service.IOrderLinesService;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.he.order.dto.OrderHeaders;
import com.he.order.service.IOrderHeadersService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrderHeadersServiceImpl extends BaseServiceImpl<OrderHeaders> implements IOrderHeadersService{
    public static final String ORDER_HEARED_ID="ORDER_HEARED_ID";
    public static final String ORDER_LINE_ID = "ORDER_LINE_ID";
    // Excel的标题栏
    private static String[] titles = new String[]{"销售订单号", "公司名称", "客户名称", "订单日期	", "订单状态", "物料编码", "物料描述", "数量", "销售单价", "金额"};
    @Autowired
    ISysCodeRuleProcessService sysCodeRuleProcessService;
    @Autowired
    OrderHeadersMapper headersMapper;
    @Autowired
    IOrderLinesService orderLinesService;
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<OrderHeaders> selectCollect(IRequest request, OrderHeaders orderHeader, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        return headersMapper.selectCollect(orderHeader);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)//事务回滚
    public List<OrderHeaders> saveHeaderAndLines(IRequest request, List<OrderHeaders> dto, int pageNum, int pageSize) throws TaskExecuteException {
        List<OrderHeaders> returnList = new ArrayList<>();
        try {
            for(OrderHeaders orderHeader:dto){
                if(orderHeader.getHeaderId()==null || orderHeader.getHeaderId()==0){
                    //插入头+行操作
                    //创建增长头ID
                    String headerCode = sysCodeRuleProcessService.getRuleCode(ORDER_HEARED_ID);
                    //将HeaderID和OrderNumber赋值
                    orderHeader.setHeaderId(Long.valueOf(headerCode));
                    orderHeader.setOrderNumber(headerCode);
                    //插入头
                    headersMapper.insert(orderHeader);
                    List<OrderLines> orderLinesList = orderHeader.getOrderLines();
                    if(orderLinesList!=null){//判断订单行是否为空
                        for(OrderLines orderLine:orderLinesList){
                            //为订单行添加HeaderID
                            orderLine.setHeaderId(orderHeader.getHeaderId());
                            //创建增长行ID
                            String lineCode=sysCodeRuleProcessService.getRuleCode(ORDER_LINE_ID);
                            orderLine.setLineId(Long.valueOf(lineCode));
                            orderLine.setLineNumber(Long.valueOf(lineCode));
                            //添加头中的CompanyID
                            orderLine.setCompanyId(orderHeader.getCompanyId());
                            //插入行
                            orderLinesService.insert(request,orderLine);
                        }
                    }
                }else {
                    //保存订单头的修改
                    headersMapper.updateByPrimaryKeySelective(orderHeader);
                    List<OrderLines> orderLinesList = orderHeader.getOrderLines();
                    //判断订单行是增加操作还是修改操作
                    if(orderLinesList!=null) {//判断订单行是否为空
                        for(OrderLines orderLine:orderLinesList){
                            if(orderLine.getLineId()!=null){//行ID不为空，修改操作
                                orderLinesService.updateByPrimaryKeySelective(request,orderLine);
                            }else {
                                //为订单行添加HeaderID
                                orderLine.setHeaderId(orderHeader.getHeaderId());
                                //创建增长行ID
                                String lineCode=sysCodeRuleProcessService.getRuleCode(ORDER_LINE_ID);
                                orderLine.setLineId(Long.valueOf(lineCode));
                                orderLine.setLineNumber(Long.valueOf(lineCode));
                                //添加头中的CompanyID
                                orderLine.setCompanyId(orderHeader.getCompanyId());
                                //插入行
                                orderLinesService.insert(request,orderLine);
                            }
                        }
                    }

                }
                returnList.add(orderHeader);
            }
        }
        catch (CodeRuleException e) {
            e.printStackTrace();
            throw new TaskExecuteException(TaskExecuteException.CODE_EXECUTE_ERROR, TaskExecuteException.MSG_SERVER_BUSY);
        }
        return returnList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteHeaderAndLines(IRequest request,List<OrderHeaders> orderHeadersList) {
        int i=0;
        if (orderHeadersList.size()>0){
            for (OrderHeaders orderHeader:orderHeadersList)
            i+=headersMapper.deleteHeaderAndLines(orderHeader);
        }
        return i;
    }

    @Override
    public SXSSFWorkbook buildExportOrderExcel(IRequest requestContext, OrderHeaders dto, int page, int pageSize) {
        if ("null".equals(dto.getOrderNumber())) {
            dto.setOrderNumber(null);
        }
        if ("null".equals(dto.getOrderStatus())) {
            dto.setOrderStatus(null);
        }
        List<OrderHeaders> orders = selectCollect(requestContext, dto, page, pageSize);
        for (OrderHeaders order : orders) {
            OrderLines temOrderLines = new OrderLines();
            temOrderLines.setHeaderId(order.getHeaderId() == null ? 0 : order.getHeaderId());
            order.setOrderLines(orderLinesService.orderDetails(requestContext, temOrderLines, page, pageSize));
        }
        SXSSFWorkbook workbook = new SXSSFWorkbook(50);
        SXSSFSheet sheet = workbook.createSheet("Order Page");
        //先用titles创建Excel的头部
        createOrderInfoExcelTitle(workbook, sheet);

        // row计数器
        final AtomicInteger count = new AtomicInteger(1);
        // sheet页row索引
        final AtomicInteger rowIndex = new AtomicInteger(1);

        createOrderInfoExcelContent(count, rowIndex, orders, workbook, sheet);

        return workbook;
    }
    /**
     * 创建导出Excel的主体部分,通过调用 createRow 来创建内容
     *  如果一个订单只存在头部,只生成一行代表这个订单的Excel行
     *  如果订单存在订单行,生成数量等于行数量的记录
     */
    private void createOrderInfoExcelContent(AtomicInteger count, AtomicInteger rowIndex, List<OrderHeaders> orders, SXSSFWorkbook workbook, SXSSFSheet sheet) {
        if (orders != null) {
            CellStyle dateFormat = workbook.createCellStyle();
            dateFormat.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-DD HH:mm:ss"));
            for (OrderHeaders order : orders) {
                if (order.getOrderLines() != null && order.getOrderLines().size() > 0) {
                    for (OrderLines orderLine : order.getOrderLines()) {
                        createRow(sheet.createRow(rowIndex.getAndIncrement()), order, orderLine, dateFormat);
                    }
                } else {
                    createRow(sheet.createRow(rowIndex.getAndIncrement()), order, null, dateFormat);
                }
            }
        }
    }
    /**
     * 循环订单头部与订单行
     */
    private void createRow(SXSSFRow row, OrderHeaders order, OrderLines orderLine, CellStyle dateFormat) {
        SXSSFCell cell0 = row.createCell(0);
        cell0.setCellType(CellType.NUMERIC);
        cell0.setCellValue(order.getOrderNumber());

        SXSSFCell cell1 = row.createCell(1);
        cell1.setCellType(CellType.STRING);
        cell1.setCellValue(order.getCompanyName());

        SXSSFCell cell2 = row.createCell(2);
        cell2.setCellType(CellType.STRING);
        cell2.setCellValue(order.getCustomerName());

        SXSSFCell cell3 = row.createCell(3);
        cell3.setCellStyle(dateFormat);
        cell3.setCellValue(order.getCustomerName());

        SXSSFCell cell4 = row.createCell(4);
        cell4.setCellType(CellType.STRING);
        cell4.setCellValue(order.getOrderStatus());

        if (orderLine == null) {
            return;
        }
        SXSSFCell cell5 = row.createCell(5);
        cell5.setCellType(CellType.STRING);
        cell5.setCellValue(orderLine.getItemCode());

        SXSSFCell cell6 = row.createCell(6);
        cell6.setCellType(CellType.STRING);
        cell6.setCellValue(orderLine.getItemDescription());

        SXSSFCell cell7 = row.createCell(7);
        cell7.setCellType(CellType.NUMERIC);
        cell7.setCellValue(orderLine.getOrderdQuantity());

        SXSSFCell cell8 = row.createCell(8);
        cell8.setCellType(CellType.NUMERIC);
        cell8.setCellValue(orderLine.getUnitSellingPrice());

        SXSSFCell cell9 = row.createCell(9);
        cell9.setCellType(CellType.NUMERIC);
        cell9.setCellValue(orderLine.getUnitSellingPrice()*orderLine.getOrderdQuantity());
        //    private static String[] titles = new String[]{"销售订单号", "公司名称", "客户名称", "订单日期	","订单状态", "物料编码", "物料描述", "数量", "销售单价", "金额"};

    }
    /**
     * 创建Excel文件的头部
     */
    private void createOrderInfoExcelTitle(SXSSFWorkbook wb,SXSSFSheet sheet) {
        SXSSFRow titleRow = sheet.createRow(0);
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        for (int i = 0; i < titles.length; i++) {
            SXSSFCell firstCell = titleRow.createCell(i);
            firstCell.setCellValue(titles[i]);
            // 设置列宽度
            sheet.setColumnWidth(i, titles[i].length() * 1000);
            firstCell.setCellStyle(cellStyle);
        }

    }
}