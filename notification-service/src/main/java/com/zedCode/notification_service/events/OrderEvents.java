package com.zedCode.notification_service.events;

import com.zedCode.notification_service.model.enums.OrderStatus;

public record OrderEvents(String orderNumber, int itemsCount, OrderStatus orderStatus) {

}
