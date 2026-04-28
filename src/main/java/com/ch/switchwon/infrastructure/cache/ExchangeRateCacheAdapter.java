package com.ch.switchwon.infrastructure.cache;

import com.ch.switchwon.application.port.out.ExchangeRateCachePort;
import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.domain.model.ExchangeRate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ExchangeRateCacheAdapter implements ExchangeRateCachePort {

    private final AtomicReference<Map<Currency, ExchangeRate>> store = new AtomicReference<>(Map.of());

    @Override
    public void putAll(Map<Currency, ExchangeRate> rates) {
        store.set(Map.copyOf(rates));
    }

    @Override
    public Optional<ExchangeRate> findByCurrency(Currency currency) {
        return Optional.ofNullable(store.get().get(currency));
    }

    @Override
    public List<ExchangeRate> findAll() {
        Map<Currency, ExchangeRate> snapshot = store.get();

        return Currency.SUPPORTED_FOREIGN.stream()
            .map(snapshot::get)
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public boolean isEmpty() {
        return store.get().isEmpty();
    }
}