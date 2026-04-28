package com.ch.switchwon.application.port.in;

import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.domain.model.Order;

import java.math.BigDecimal;

public interface CreateOrderUseCase {

    Order createOrder(Currency fromCurrency, Currency toCurrency, BigDecimal forexAmount);
}
