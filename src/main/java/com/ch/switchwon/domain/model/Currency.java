package com.ch.switchwon.domain.model;

import com.ch.switchwon.domain.exception.CurrencyNotFoundException;

import java.util.List;
import java.util.Locale;

public enum Currency {
    KRW,
    USD,
    JPY,
    CNY,
    EUR;

    public static final List<Currency> SUPPORTED_FOREIGN = List.of(USD, JPY, CNY, EUR);

    public static Currency of(String code) {
        if (code == null || code.isBlank()) {
            throw new CurrencyNotFoundException(code);
        }

        try {
            return valueOf(code.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CurrencyNotFoundException(code);
        }
    }
}
