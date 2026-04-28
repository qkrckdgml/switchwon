package com.ch.switchwon.presentation.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderRequest(

    @NotNull(message = "forexAmount 는 필수입니다.")
    @DecimalMin(value = "0.0001", message = "forexAmount 는 0보다 커야 합니다.")
    BigDecimal forexAmount,

    @NotNull(message = "fromCurrency 는 필수입니다.")
    String fromCurrency,

    @NotNull(message = "toCurrency 는 필수입니다.")
    String toCurrency
) {}
