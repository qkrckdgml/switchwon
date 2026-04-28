package com.ch.switchwon.application.port.out;

import com.ch.switchwon.domain.model.Currency;

import java.math.BigDecimal;
import java.util.Map;

public interface ExchangeRateApiPort {

    Map<Currency, BigDecimal> fetchBaseRates();
}
