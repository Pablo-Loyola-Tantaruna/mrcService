package com.zedCode.notification_service.listeners;

import com.zedCode.notification_service.events.OrderEvents;
import com.zedCode.notification_service.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventListener {

    @KafkaListener(topics = "orders-topic")
    public void handlerOrderNotifications(String message){
        var orderEvent = JsonUtils.fromJson(message, OrderEvents.class);
        //Send email to customer, send SMS to customer, etc.
        //Notify another service...


        log.info("Order ${orderEvent.orderStatus()} event received for order: {} with {} items", orderEvent.orderNumber(), orderEvent.itemsCount());
    }
}
