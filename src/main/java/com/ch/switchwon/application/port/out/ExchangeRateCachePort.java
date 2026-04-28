package com.ch.switchwon.application.port.out;

import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.domain.model.ExchangeRate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ExchangeRateCachePort {

    void putAll(Map<Currency, ExchangeRate> rates);

    Optional<ExchangeRate> findByCurrency(Currency currency);

    List<ExchangeRate> findAll();

    boolean isEmpty();
}
