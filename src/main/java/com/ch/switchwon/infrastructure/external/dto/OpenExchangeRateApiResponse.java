package com.ch.switchwon.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

public record OpenExchangeRateApiResponse(
    String result,

    @JsonProperty("base_code")
    String baseCode,

    Map<String, BigDecimal> rates
) {
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(result);
    }
}
