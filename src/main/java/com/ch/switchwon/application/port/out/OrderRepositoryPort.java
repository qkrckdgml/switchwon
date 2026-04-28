package com.ch.switchwon.application.port.out;

import com.ch.switchwon.domain.model.Order;

import java.util.List;

public interface OrderRepositoryPort {

    Order save(Order order);

    List<Order> findAll();
}
