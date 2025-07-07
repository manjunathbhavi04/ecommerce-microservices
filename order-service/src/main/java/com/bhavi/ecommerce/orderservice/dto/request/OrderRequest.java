package com.bhavi.ecommerce.orderservice.dto.request;

import com.bhavi.ecommerce.orderservice.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    private String customerEmail;

    private List<OrderItemRequest> orderItems;
}
