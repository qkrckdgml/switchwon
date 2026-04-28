package com.ch.switchwon.presentation.web.dto;

import com.ch.switchwon.domain.model.ExchangeRate;

import java.util.List;

public record ExchangeRateListResponse(List<ExchangeRateResponse> exchangeRateList) {

    public static ExchangeRateListResponse from(List<ExchangeRate> rates) {
        return new ExchangeRateListResponse(
            rates.stream().map(ExchangeRateResponse::from).toList()
        );
    }
}
