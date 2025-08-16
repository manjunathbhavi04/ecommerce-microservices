package com.bhavi.ecommerce.orderservice.service;

import com.bhavi.ecommerce.orderservice.dto.request.OrderItemRequest;
import com.bhavi.ecommerce.orderservice.dto.request.OrderRequest;
import com.bhavi.ecommerce.orderservice.dto.response.OrderResponse;
import com.bhavi.ecommerce.orderservice.dto.response.ProductResponseDto;
import com.bhavi.ecommerce.orderservice.enums.Status;
import com.bhavi.ecommerce.orderservice.exception.InsufficientProductStock;
import com.bhavi.ecommerce.orderservice.exception.OrderNotFoundException;
import com.bhavi.ecommerce.orderservice.exception.ProductNotFoundException;
import com.bhavi.ecommerce.orderservice.model.Order;
import com.bhavi.ecommerce.orderservice.model.OrderItem;
import com.bhavi.ecommerce.orderservice.repository.OrderItemRepository;
import com.bhavi.ecommerce.orderservice.repository.OrderRepository;
import com.bhavi.ecommerce.orderservice.service.client.ProductServiceClient;
import com.bhavi.ecommerce.orderservice.transformer.OrderItemTransformer;
import com.bhavi.ecommerce.orderservice.transformer.OrderTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductServiceClient productServiceClient;

    @Transactional
    public OrderResponse placeOrder(OrderRequest ord) {

        List<OrderItem> items = new ArrayList<>();

        for(OrderItemRequest item: ord.getOrderItems()) {
            log.info("Trying to get the product detail");
            ProductResponseDto product = productServiceClient.getProductDetails(item.getProductId()).block();
            if(product == null) {
                throw new ProductNotFoundException("Invalid product");
            } else {
                log.info("Got the product details");
            }
            if(item.getQuantity() > product.getStockQuantity()) {
                throw new InsufficientProductStock("Out of stock");
            } else {
                log.info("Order Service attempting to decrease the product quantity");
                productServiceClient.decreaseProductStock(product.getId(), item.getQuantity()).block();
                log.info("Success with product decrease quantity");
            }
            items.add(OrderItemTransformer.orderItemRequestToOrderItem(item, product));
        }
        Order order = OrderTransformer.orderRequestToOrder(ord);
        order.setOrderItems(items);

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

        Order savedOrder =  orderRepository.save(order);

        return OrderTransformer.orderToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderId(Long orderId) {
        return OrderTransformer.orderToOrderResponse(
                orderRepository.findById(orderId).orElseThrow(
                        () -> new OrderNotFoundException("Invalid Order Id: "+ orderId)
                )
        );
    }

    private String generateNumber() {
        // very low probability of getting the same number
        return UUID.randomUUID().toString();
    }
}
