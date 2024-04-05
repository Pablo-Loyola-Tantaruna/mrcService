package com.zedcode.orders_service.services;

import com.zedcode.orders_service.events.OrderEvents;
import com.zedcode.orders_service.model.dtos.*;
import com.zedcode.orders_service.model.entities.Order;
import com.zedcode.orders_service.model.entities.OrderItems;
import com.zedcode.orders_service.model.enums.OrderStatus;
import com.zedcode.orders_service.repositories.OrderRepository;
import com.zedcode.orders_service.utils.JsonUtils;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObservationRegistry observationRegistry;


    public OrderResponse placeOrder(OrderRequest orderRequest){

        Observation inventoryObservation = Observation.createNotStarted("inventory-service", observationRegistry);
        //Check for inventory
        return inventoryObservation.observe(()->{
            BaseResponse result = this.webClientBuilder.build()
                    .post()
                    .uri("lb://inventory_service/api/inventory/in-stock")
                    .bodyValue(orderRequest.getOrderItemsList())
                    .retrieve()
                    .bodyToMono(BaseResponse.class)
                    .block();

            if(result!=null && !result.hasErrors()) {
                Order order = new Order();
                order.setOrderNumber(UUID.randomUUID().toString());
                order.setOrderItemsList(orderRequest.getOrderItemsList().stream()
                        .map(orderItemRequest -> mapOrderItemRequestToOrderItem(orderItemRequest, order)).toList());
                var savedOrder = this.orderRepository.save(order);
                //TODO: Send message to order topic
                this.kafkaTemplate.send("orders-topic", JsonUtils.toJson(
                        new OrderEvents(savedOrder.getOrderNumber(), savedOrder.getOrderItemsList().size(), OrderStatus.PLACED)
                ));
                return mapToOrderResponse(savedOrder);
            }else {
                throw new IllegalArgumentException("Some of the products are not in stock");
            }
        });
    }

    public List<OrderResponse> getAllOrders(){
        List<Order> orders = this.orderRepository.findAll();
        return orders.stream().map(this::mapToOrderResponse).toList();
    }

    private OrderResponse mapToOrderResponse(Order order){
        return new OrderResponse(order.getId(), order.getOrderNumber(),
                order.getOrderItemsList().stream().map(this::mapToOrderItemRequest).toList());
    }

    private OrderItemsResponse mapToOrderItemRequest(OrderItems orderItems){
        return new OrderItemsResponse(orderItems.getId(),orderItems.getSku(), orderItems.getPrice(), orderItems.getQuantity());
    }

    private OrderItems mapOrderItemRequestToOrderItem(OrderItemRequest orderItemRequest, Order order){

        return OrderItems.builder()
                .id(orderItemRequest.getId())
                .sku(orderItemRequest.getSku())
                .price(orderItemRequest.getPrice())
                .quantity(orderItemRequest.getQuantity())
                .order(order)
                .build();
    }
}
