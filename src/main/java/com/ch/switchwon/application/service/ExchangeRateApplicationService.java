package com.ch.switchwon.application.service;

import com.ch.switchwon.application.port.in.GetLatestExchangeRateUseCase;
import com.ch.switchwon.application.port.in.GetLatestExchangeRatesUseCase;
import com.ch.switchwon.application.port.in.RefreshExchangeRatesUseCase;
import com.ch.switchwon.application.port.out.ExchangeRateApiPort;
import com.ch.switchwon.application.port.out.ExchangeRateCachePort;
import com.ch.switchwon.application.port.out.ExchangeRateRepositoryPort;
import com.ch.switchwon.domain.exception.ExchangeRateNotFoundException;
import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.domain.model.ExchangeRate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateApplicationService implements
    GetLatestExchangeRatesUseCase,
    GetLatestExchangeRateUseCase,
    RefreshExchangeRatesUseCase
{
    private final ExchangeRateApiPort exchangeRateApiPort;
    private final ExchangeRateCachePort exchangeRateCachePort;
    private final ExchangeRateRepositoryPort exchangeRateRepositoryPort;

    @Override
    public List<ExchangeRate> getLatestRates() {
        List<ExchangeRate> cached = exchangeRateCachePort.findAll();
        return cached.isEmpty() ? exchangeRateRepositoryPort.findAllLatest() : cached;
    }

    @Override
    public ExchangeRate getLatestRate(Currency currency) {
        return exchangeRateCachePort.findByCurrency(currency)
            .or(() -> exchangeRateRepositoryPort.findLatestByCurrency(currency))
            .orElseThrow(() -> new ExchangeRateNotFoundException(currency));
    }

    @Override
    public void refresh() {
        Map<Currency, BigDecimal> baseRates = exchangeRateApiPort.fetchBaseRates();
        if (baseRates.isEmpty()) {
            log.warn("[환율 갱신] 외부 API 응답 Empty - 갱신 skip");
            return;
        }

        LocalDateTime collectedAt = LocalDateTime.now();
        Map<Currency, ExchangeRate> newSnapshot = new EnumMap<>(Currency.class);
        baseRates.forEach((currency, base) ->
            newSnapshot.put(currency, ExchangeRate.of(currency, base, collectedAt))
        );

        exchangeRateRepositoryPort.saveAll(newSnapshot.values());
        exchangeRateCachePort.putAll(newSnapshot);

        log.info("[환율 갱신] 완료 - {}개 통화 (수집 시각: {})", newSnapshot.size(), collectedAt);
    }
}