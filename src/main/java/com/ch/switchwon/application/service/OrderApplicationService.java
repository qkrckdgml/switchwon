package com.ch.switchwon.application.service;

import com.ch.switchwon.application.port.in.CreateOrderUseCase;
import com.ch.switchwon.application.port.in.GetOrderListUseCase;
import com.ch.switchwon.application.port.out.ExchangeRateCachePort;
import com.ch.switchwon.application.port.out.ExchangeRateRepositoryPort;
import com.ch.switchwon.application.port.out.OrderRepositoryPort;
import com.ch.switchwon.domain.exception.ExchangeRateNotFoundException;
import com.ch.switchwon.domain.exception.InvalidOrderRequestException;
import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.domain.model.ExchangeRate;
import com.ch.switchwon.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderApplicationService implements CreateOrderUseCase, GetOrderListUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ExchangeRateCachePort exchangeRateCachePort;
    private final ExchangeRateRepositoryPort exchangeRateRepositoryPort;

    @Override
    public Order createOrder(Currency fromCurrency, Currency toCurrency, BigDecimal forexAmount) {
        validateOrderCurrencies(fromCurrency, toCurrency);

        Currency foreignCurrency = (fromCurrency == Currency.KRW) ? toCurrency : fromCurrency;
        ExchangeRate rate = exchangeRateCachePort.findByCurrency(foreignCurrency)
            .or(() -> exchangeRateRepositoryPort.findLatestByCurrency(foreignCurrency))
            .orElseThrow(() -> new ExchangeRateNotFoundException(foreignCurrency));

        return orderRepositoryPort.save(Order.create(fromCurrency, toCurrency, forexAmount, rate));
    }

    @Override
    public List<Order> getOrders() {
        return orderRepositoryPort.findAll();
    }

    private void validateOrderCurrencies(Currency from, Currency to) {
        if (from == to) {
            throw new InvalidOrderRequestException("통화(from-to)는 같을 수 없습니다.");
        }
        if (from != Currency.KRW && to != Currency.KRW) {
            throw new InvalidOrderRequestException("매수 또는 매도 주문 중 하나여야 합니다.");
        }
    }
}