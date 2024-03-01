package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 统计营业额
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverReportStatistic(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        List<Double> amoutList=new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        for(LocalDate date:dateList){
            LocalDateTime localDateTime1=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime localDateTime2=LocalDateTime.of(date, LocalTime.MAX);

            Integer status = Orders.COMPLETED;

            Double sum=orderMapper.sumBy3(localDateTime1,localDateTime2,status);
            if(sum==null) sum=0.;
            amoutList.add(sum);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(amoutList,","))
                .build();
    }

    /**
     * 统计用户数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        List<Integer> newUser=new ArrayList<>();
        List<Integer> User=new ArrayList<>();



        dateList.add(begin);
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        for(LocalDate date:dateList){
            LocalDateTime localDateTime1=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime localDateTime2=LocalDateTime.of(date, LocalTime.MAX);
            Integer newSum=userMapper.sumNew(localDateTime1,localDateTime2);
            Integer sum=userMapper.sum(localDateTime2);
            if(newSum==null) newSum=0;
            if(sum==null) sum=0;
            newUser.add(newSum);
            User.add(sum);
        }


        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUser,","))
                .totalUserList(StringUtils.join(User,","))
                .build();
    }

    /**
     * 订单数据统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {

        LocalDateTime localDateTime_begin=LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime localDateTime_end=LocalDateTime.of(end, LocalTime.MAX);

        // 时间列表
        List<LocalDate> dateList=new ArrayList<>();
        //订单总数
        Integer totalOrderCount=orderMapper.count(localDateTime_begin,localDateTime_end);
        //有效订单数
        Integer validOrderCount=orderMapper.countValid(localDateTime_begin,localDateTime_end,Orders.COMPLETED);
        //订单完成率
        double orderCompletionRate;
        if(totalOrderCount==0) orderCompletionRate=0.;
        else orderCompletionRate=validOrderCount/(totalOrderCount*1.0);

        //订单数列表
        List<Integer> orderCountList=new ArrayList<>();
        //有效订单数列表
        List<Integer> validOrderCountList=new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        for(LocalDate date:dateList){
            LocalDateTime localDateTime1=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime localDateTime2=LocalDateTime.of(date, LocalTime.MAX);
            Integer validOrderSum=orderMapper.countValid(localDateTime1,localDateTime2,Orders.COMPLETED);
            Integer orderSum=orderMapper.count(localDateTime1,localDateTime2);
            if(validOrderSum==null) validOrderSum=0;
            if(orderSum==null) orderSum=0;
            orderCountList.add(orderSum);
            validOrderCountList.add(validOrderSum);
        }


        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .validOrderCount(validOrderCount)
                .totalOrderCount(totalOrderCount)
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO salesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime localDateTime_begin=LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime localDateTime_end=LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesList=orderDetailMapper.getNameAndNumber(localDateTime_begin,localDateTime_end,Orders.COMPLETED);
        List<String> name = goodsSalesList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> number = goodsSalesList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(name,","))
                .numberList(StringUtils.join(number,","))
                .build();
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
