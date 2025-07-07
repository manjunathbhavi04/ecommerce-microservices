package com.bhavi.ecommerce.orderservice.dto.response;


import com.bhavi.ecommerce.orderservice.enums.Status;
import com.bhavi.ecommerce.orderservice.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String orderTrackingNumber;

    private String customerEmail;

    private Status status;

    private LocalDateTime orderDate;

    private BigDecimal totalPrice;

    private Integer totalQuantity;

    private List<OrderItemResponse> orderItems;
}
