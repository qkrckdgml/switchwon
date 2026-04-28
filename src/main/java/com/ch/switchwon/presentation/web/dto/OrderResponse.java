package com.ch.switchwon.presentation.web.dto;

import com.ch.switchwon.domain.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
    Long id,
    BigDecimal fromAmount,
    String fromCurrency,
    BigDecimal toAmount,
    String toCurrency,
    BigDecimal tradeRate,
    LocalDateTime dateTime
) {
    public static OrderResponse from(Order domain) {
        return new OrderResponse(
            domain.id(),
            domain.fromAmount(),
            domain.fromCurrency().name(),
            domain.toAmount(),
            domain.toCurrency().name(),
            domain.tradeRate(),
            domain.dateTime()
        );
    }
}
