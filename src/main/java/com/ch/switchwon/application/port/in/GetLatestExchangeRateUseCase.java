package com.ch.switchwon.application.port.in;

import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.domain.model.ExchangeRate;

public interface GetLatestExchangeRateUseCase {

    ExchangeRate getLatestRate(Currency currency);
}
