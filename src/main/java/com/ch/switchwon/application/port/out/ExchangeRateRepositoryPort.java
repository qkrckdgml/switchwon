package com.ch.switchwon.application.port.out;

import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.domain.model.ExchangeRate;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepositoryPort {

    void save(ExchangeRate exchangeRate);

    void saveAll(Collection<ExchangeRate> exchangeRates);

    Optional<ExchangeRate> findLatestByCurrency(Currency currency);

    List<ExchangeRate> findAllLatest();
}
