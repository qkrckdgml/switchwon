package com.ch.switchwon.domain.model;

import com.ch.switchwon.domain.exception.InvalidOrderRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public record Order(
    Long id,
    BigDecimal fromAmount,
    Currency fromCurrency,
    BigDecimal toAmount,
    Currency toCurrency,
    BigDecimal tradeRate,
    LocalDateTime dateTime
) {
    public static Order create(
        Currency fromCurrency,
        Currency toCurrency,
        BigDecimal forexAmount,
        ExchangeRate rate
    ) {
        if (forexAmount == null || forexAmount.signum() <= 0) {
            throw new InvalidOrderRequestException("외화 금액은 0보다 커야 합니다.");
        }

        boolean isBuy = (fromCurrency == Currency.KRW);
        BigDecimal applied = isBuy ? rate.buyRate() : rate.sellRate();
        BigDecimal krwAmount = forexAmount.multiply(applied).setScale(0, RoundingMode.FLOOR);
        BigDecimal fxAmount = forexAmount.setScale(2, RoundingMode.HALF_UP);

        return isBuy
            ? new Order(null, krwAmount, Currency.KRW, fxAmount, toCurrency, applied, LocalDateTime.now())
            : new Order(null, fxAmount, fromCurrency, krwAmount, Currency.KRW, applied, LocalDateTime.now());
    }
}