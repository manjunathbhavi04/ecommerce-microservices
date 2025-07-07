package com.bhavi.ecommerce.orderservice.transformer;

import com.bhavi.ecommerce.orderservice.dto.request.OrderItemRequest;
import com.bhavi.ecommerce.orderservice.dto.response.OrderItemResponse;
import com.bhavi.ecommerce.orderservice.model.OrderItem;

import java.math.BigDecimal;

public class OrderItemTransformer {
    public static OrderItem orderItemRequestToOrderItem(OrderItemRequest orderItemRequest) {
        return OrderItem.builder()
                .imageUrl(orderItemRequest.getImageUrl())
                .productId(orderItemRequest.getProductId())
                .quantity(orderItemRequest.getQuantity())
                .productName("#####") // for now
                .unitPrice(BigDecimal.TEN) // for now
                .build();
    }

    public static OrderItemResponse orderItemToOrderItemResponse(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .imageUrl(orderItem.getImageUrl())
                .productName(orderItem.getProductName())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .build();
    }
}
