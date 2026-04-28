package com.ch.switchwon.presentation.web;

import com.ch.switchwon.application.port.in.GetLatestExchangeRateUseCase;
import com.ch.switchwon.application.port.in.GetLatestExchangeRatesUseCase;
import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.presentation.web.api.ExchangeRateApiContract;
import com.ch.switchwon.presentation.web.dto.ApiResponse;
import com.ch.switchwon.presentation.web.dto.ExchangeRateListResponse;
import com.ch.switchwon.presentation.web.dto.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ExchangeRateController implements ExchangeRateApiContract {

    private final GetLatestExchangeRatesUseCase getLatestExchangeRatesUseCase;
    private final GetLatestExchangeRateUseCase getLatestExchangeRateUseCase;

    @Override
    public ApiResponse<ExchangeRateListResponse> getLatestRates() {
        return ApiResponse.ok(
            ExchangeRateListResponse.from(getLatestExchangeRatesUseCase.getLatestRates())
        );
    }

    @Override
    public ApiResponse<ExchangeRateResponse> getLatestRate(
        String currency
    ) {
        return ApiResponse.ok(
            ExchangeRateResponse.from(
                getLatestExchangeRateUseCase.getLatestRate(Currency.of(currency))
            )
        );
    }
}