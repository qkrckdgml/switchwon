package com.ch.switchwon.presentation.web.dto;

import com.ch.switchwon.domain.model.ExchangeRate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExchangeRateResponse(
    String currency,
    BigDecimal buyRate,
    BigDecimal tradeStanRate,
    BigDecimal sellRate,
    LocalDateTime dateTime
) {
    public static ExchangeRateResponse from(ExchangeRate domain) {
        return new ExchangeRateResponse(
            domain.currency().name(),
            domain.buyRate(),
            domain.tradeStanRate(),
            domain.sellRate(),
            domain.dateTime()
        );
    }
}
