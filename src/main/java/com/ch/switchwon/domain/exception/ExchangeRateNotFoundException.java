package com.ch.switchwon.domain.exception;

import com.ch.switchwon.domain.model.Currency;

public class ExchangeRateNotFoundException extends RuntimeException {

    public ExchangeRateNotFoundException(Currency currency) {
        super(currency.name() + " 환율 정보를 찾을 수 없습니다.");
    }
}
