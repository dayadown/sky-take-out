package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
