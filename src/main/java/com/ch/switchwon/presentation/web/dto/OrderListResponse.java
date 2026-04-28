package com.ch.switchwon.presentation.web.dto;

import com.ch.switchwon.domain.model.Order;

import java.util.List;

public record OrderListResponse(List<OrderResponse> orderList) {

    public static OrderListResponse from(List<Order> orders) {
        return new OrderListResponse(
            orders.stream().map(OrderResponse::from).toList()
        );
    }
}
