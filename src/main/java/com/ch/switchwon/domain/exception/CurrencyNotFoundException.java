package com.ch.switchwon.domain.exception;

public class CurrencyNotFoundException extends RuntimeException {

    public CurrencyNotFoundException(String currencyCode) {
        super("지원하지 않는 통화 코드입니다: " + currencyCode);
    }
}
