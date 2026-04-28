package com.ch.switchwon.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public record ExchangeRate(
    Long id,
    Currency currency,
    BigDecimal tradeStanRate,
    BigDecimal buyRate,
    BigDecimal sellRate,
    LocalDateTime dateTime
) {
    private static final BigDecimal BUY_SPREAD  = new BigDecimal("1.05");
    private static final BigDecimal SELL_SPREAD = new BigDecimal("0.95");
    private static final int SCALE = 2;

    public static ExchangeRate of(Currency currency, BigDecimal tradeStanRate, LocalDateTime collectedAt) {
        BigDecimal stan = tradeStanRate.setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal buy  = tradeStanRate.multiply(BUY_SPREAD ).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal sell = tradeStanRate.multiply(SELL_SPREAD).setScale(SCALE, RoundingMode.HALF_UP);

        return new ExchangeRate(null, currency, stan, buy, sell, collectedAt);
    }

    public static ExchangeRate of(Currency currency, BigDecimal tradeStanRate) {
        return of(currency, tradeStanRate, LocalDateTime.now());
    }
}
