package com.bhavi.ecommerce.orderservice.transformer;

import com.bhavi.ecommerce.orderservice.dto.request.OrderItemRequest;
import com.bhavi.ecommerce.orderservice.dto.request.OrderRequest;
import com.bhavi.ecommerce.orderservice.dto.response.OrderItemResponse;
import com.bhavi.ecommerce.orderservice.dto.response.OrderResponse;
import com.bhavi.ecommerce.orderservice.model.Order;
import com.bhavi.ecommerce.orderservice.model.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderTransformer {
    public static Order orderRequestToOrder(OrderRequest order) {
        List<OrderItem> items = new ArrayList<>();

        for(OrderItemRequest item: order.getOrderItems()) {
            items.add(OrderItemTransformer.orderItemRequestToOrderItem(item));
        }
        return Order.builder()
                .customerEmail(order.getCustomerEmail())
                .orderItems(items)
                .build();
    }

    public static OrderResponse orderToOrderResponse(Order order) {
        List<OrderItemResponse> items = new ArrayList<>();
        for(OrderItem item: order.getOrderItems()) {
            items.add(OrderItemTransformer.orderItemToOrderItemResponse(item));
        }
        return OrderResponse.builder()
                .orderTrackingNumber(order.getOrderTrackingNumber())
                .customerEmail(order.getCustomerEmail())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .totalQuantity(order.getTotalQuantity())
                .totalPrice(order.getTotalPrice())
                .orderItems(items)
                .build();
    }
}
