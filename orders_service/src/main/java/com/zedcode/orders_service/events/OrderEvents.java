package com.zedcode.orders_service.events;

import com.zedcode.orders_service.model.enums.OrderStatus;

public record OrderEvents(String orderNumber, int itemsCount, OrderStatus orderStatus) {

}
