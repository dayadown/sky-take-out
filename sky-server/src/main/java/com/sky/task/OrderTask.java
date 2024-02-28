package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void timeoutOrder(){
        List<Orders> ordersList=orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        if(ordersList!=null && !ordersList.isEmpty()) {
            for (Orders o : ordersList) {
                o.setStatus(Orders.CANCELLED);
                o.setCancelReason("订单超时");
                o.setCancelTime(LocalDateTime.now());
                orderMapper.update(o);
            }
        }
    }
    /**
     * 处理一直处于派送中的订单，自动完成
     * 订单需要在凌晨1点派送完成，否则自动完成(改进)
     * TODO 商家打烊后一个小时还处于派送中的订单自动完成
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void dOrder(){
        List<Orders> ordersList=orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));
        if(ordersList!=null && !ordersList.isEmpty()) {
            for (Orders o : ordersList) {
                o.setStatus(Orders.COMPLETED);
                orderMapper.update(o);
            }
        }
    }


}
