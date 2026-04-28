package com.ch.switchwon.application.port.in;

import com.ch.switchwon.domain.model.Order;

import java.util.List;

public interface GetOrderListUseCase {

    List<Order> getOrders();
}
