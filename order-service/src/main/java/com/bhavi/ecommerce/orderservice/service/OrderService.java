package com.bhavi.ecommerce.orderservice.service;

import com.bhavi.ecommerce.orderservice.enums.Status;
import com.bhavi.ecommerce.orderservice.exception.OrderNotFoundException;
import com.bhavi.ecommerce.orderservice.model.Order;
import com.bhavi.ecommerce.orderservice.model.OrderItem;
import com.bhavi.ecommerce.orderservice.repository.OrderItemRepository;
import com.bhavi.ecommerce.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public Order placeOrder(Order order) {

        // Generating unique tracking order number
        order.setOrderTrackingNumber(generateNumber());

        // Initial Status of any order
        order.setStatus(Status.PENDING);

        BigDecimal totalOrderPrice = BigDecimal.ZERO;
        int totalOrderQuantity = 0;

        // One order can have multiple items so save them separately
        for(OrderItem item: order.getOrderItems()) {

            // set the item to its related order meaning which order does this item belong to
            item.setOrder(order);

            // calculate the total price of the order
            totalOrderPrice = totalOrderPrice.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            // total quantity of the order
            totalOrderQuantity += item.getQuantity();
        }

        order.setTotalQuantity(totalOrderQuantity);
        order.setTotalPrice(totalOrderPrice);

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderByOrderId(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Invalid Order Id: "+ orderId));
    }

    private String generateNumber() {
        // very low probability of getting the same number
        return UUID.randomUUID().toString();
    }
}
