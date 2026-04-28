package com.ch.switchwon.application.port.in;

import com.ch.switchwon.domain.model.ExchangeRate;

import java.util.List;

public interface GetLatestExchangeRatesUseCase {

    List<ExchangeRate> getLatestRates();
}
