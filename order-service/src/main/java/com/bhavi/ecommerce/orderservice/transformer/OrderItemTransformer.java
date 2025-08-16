package com.bhavi.ecommerce.orderservice.transformer;

import com.bhavi.ecommerce.orderservice.dto.request.OrderItemRequest;
import com.bhavi.ecommerce.orderservice.dto.response.OrderItemResponse;
import com.bhavi.ecommerce.orderservice.dto.response.ProductResponseDto;
import com.bhavi.ecommerce.orderservice.model.OrderItem;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderItemTransformer {
    public static OrderItem orderItemRequestToOrderItem(OrderItemRequest orderItemRequest, ProductResponseDto product) {
        return OrderItem.builder()
                .imageUrl(orderItemRequest.getImageUrl())
                .productId(orderItemRequest.getProductId())
                .quantity(orderItemRequest.getQuantity())
                .productName(product.getName())
                .unitPrice(product.getPrice())
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
