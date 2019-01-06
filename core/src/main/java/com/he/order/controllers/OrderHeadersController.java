package com.he.order.controllers;

import com.hand.hap.task.exception.TaskExecuteException;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Controller;
import com.hand.hap.system.controllers.BaseController;
import com.hand.hap.core.IRequest;
import com.hand.hap.system.dto.ResponseData;
import com.he.order.dto.OrderHeaders;
import com.he.order.service.IOrderHeadersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindingResult;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

    @Controller
    public class OrderHeadersController extends BaseController{

    private static final String ENCODING = "UTF-8";
    @Autowired
    private IOrderHeadersService service;

    @RequestMapping(value = "/hap/om/order/headers/saveHeaderAndLines")
    @ResponseBody
    public ResponseData saveHeaderAndLines(@RequestBody List<OrderHeaders> dto, @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                              @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request) throws Exception {
        IRequest requestContext = createRequestContext(request);
        return new ResponseData(service.saveHeaderAndLines(requestContext,dto,page,pageSize));
    }

    @RequestMapping(value = "/hap/om/order/headers/query")
    @ResponseBody
    public ResponseData query(OrderHeaders dto, @RequestParam(defaultValue = DEFAULT_PAGE) int page,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request) {
        IRequest requestContext = createRequestContext(request);
        return new ResponseData(service.selectCollect(requestContext,dto,page,pageSize));
    }

    @RequestMapping(value = "/hap/om/order/headers/submit")
    @ResponseBody
    public ResponseData update(@RequestBody List<OrderHeaders> dto, BindingResult result, HttpServletRequest request){
        getValidator().validate(dto, result);
        if (result.hasErrors()) {
        ResponseData responseData = new ResponseData(false);
        responseData.setMessage(getErrorMessage(result, request));
        return responseData;
        }
        IRequest requestCtx = createRequestContext(request);
        return new ResponseData(service.batchUpdate(requestCtx, dto));
    }

    @RequestMapping(value = "/hap/om/order/headers/remove")
    @ResponseBody
    public ResponseData delete(HttpServletRequest request,@RequestBody List<OrderHeaders> dto){
        service.batchDelete(dto);
        return new ResponseData();
    }
    @RequestMapping(value = "/hap/om/order/headers/deleteOrder")
    public ResponseData deleteHeaderAndLines(HttpServletRequest request,@RequestBody List<OrderHeaders> dto){
        IRequest requestCtx = createRequestContext(request);
        service.deleteHeaderAndLines(requestCtx,dto);
        return new ResponseData(new ArrayList<>());
    }
    /**
     * 导出Excel
     */
    @RequestMapping(value = "/hap/om/order/headers/exportOrderExcel")
    public void exportOrderExcel(OrderHeaders dto, @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                                 @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        IRequest requestContext = createRequestContext(httpServletRequest);
        String name = "Order-Info.xlsx";
        String userAgent = httpServletRequest.getHeader("User-Agent");
        if (userAgent.contains("Firefox")) {
            name = new String(name.getBytes("UTF-8"), "ISO8859-1");
        } else {
            name = URLEncoder.encode(name, ENCODING);
        }
        httpServletResponse.addHeader("Content-Disposition",
                "attachment; filename=\"" + name + "\"");
        httpServletResponse.setContentType("application/vnd.ms-excel" + ";charset=" + ENCODING);
        httpServletResponse.setHeader("Accept-Ranges", "bytes");
        SXSSFWorkbook excelFile = service.buildExportOrderExcel(requestContext, dto, page, pageSize);
        try (ServletOutputStream outputStream = httpServletResponse.getOutputStream();) {
            excelFile.write(outputStream);
        } finally {
            excelFile.close();
            excelFile.dispose();
        }
    }
    }