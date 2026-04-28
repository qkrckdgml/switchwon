package com.ch.switchwon.infrastructure.persistence.repository;

import com.ch.switchwon.domain.model.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OrderView {
    Long getId();
    BigDecimal getFromAmount();
    Currency getFromCurrency();
    BigDecimal getToAmount();
    Currency getToCurrency();
    BigDecimal getTradeRate();
    LocalDateTime getDateTime();
}
