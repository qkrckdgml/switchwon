package com.ch.switchwon.presentation.web;

import com.ch.switchwon.application.port.in.GetLatestExchangeRateUseCase;
import com.ch.switchwon.application.port.in.GetLatestExchangeRatesUseCase;
import com.ch.switchwon.config.JacksonConfig;
import com.ch.switchwon.domain.exception.CurrencyNotFoundException;
import com.ch.switchwon.domain.exception.ExchangeRateNotFoundException;
import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.domain.model.ExchangeRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeRateController.class)
@Import({GlobalExceptionHandler.class, JacksonConfig.class})
@DisplayName("Exchange rate controller test")
class ExchangeRateControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    GetLatestExchangeRatesUseCase getLatestExchangeRatesUseCase;

    @MockitoBean
    GetLatestExchangeRateUseCase getLatestExchangeRateUseCase;

    @Test
    @DisplayName("GET /exchange-rate/latest -> 전체 환율 목록 반환 -> 성공")
    void get_latest_rate_return_200() throws Exception {
        List<ExchangeRate> rates = List.of(
            ExchangeRate.of(Currency.USD, new BigDecimal("1477.45")),
            ExchangeRate.of(Currency.JPY, new BigDecimal("923.78"))
        );

        given(getLatestExchangeRatesUseCase.getLatestRates()).willReturn(rates);

        mockMvc.perform(get("/exchange-rate/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("OK"))
            .andExpect(jsonPath("$.returnObject.exchangeRateList").isArray())
            .andExpect(jsonPath("$.returnObject.exchangeRateList.length()").value(2))
            .andExpect(jsonPath("$.returnObject.exchangeRateList[0].currency").value("USD"))
            .andExpect(jsonPath("$.returnObject.exchangeRateList[0].buyRate").value(1551.32))
            .andExpect(jsonPath("$.returnObject.exchangeRateList[0].sellRate").value(1403.58));
    }

    @Test
    @DisplayName("GET /exchange-rate/latest -> 데이터 X -> 빈 목록 반환")
    void get_latest_rate_return_200_empty_list() throws Exception {
        given(getLatestExchangeRatesUseCase.getLatestRates()).willReturn(List.of());

        mockMvc.perform(get("/exchange-rate/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.returnObject.exchangeRateList").isEmpty());
    }

    @Test
    @DisplayName("GET /exchange-rate/latest/USD -> USD 환율 -> 200 반환")
    void get_latest_rate_usd_return_200() throws Exception {
        ExchangeRate rate = ExchangeRate.of(Currency.USD, new BigDecimal("1477.45"));

        given(getLatestExchangeRateUseCase.getLatestRate(Currency.USD)).willReturn(rate);

        mockMvc.perform(get("/exchange-rate/latest/USD"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("OK"))
            .andExpect(jsonPath("$.returnObject.currency").value("USD"))
            .andExpect(jsonPath("$.returnObject.tradeStanRate").value(1477.45));
    }

    @Test
    @DisplayName("GET /exchange-rate/latest/jpy -> 소문자 통화 코드 -> 정상 처리")
    void get_latest_rate_lowercase_return_200() throws Exception {
        ExchangeRate rate = ExchangeRate.of(Currency.JPY, new BigDecimal("923.78"));

        given(getLatestExchangeRateUseCase.getLatestRate(Currency.JPY)).willReturn(rate);

        mockMvc.perform(get("/exchange-rate/latest/jpy"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.returnObject.currency").value("JPY"));
    }

    @Test
    @DisplayName("GET /exchange-rate/latest/ABC -> 지원하지 않는 통화 -> 400 반환")
    void get_latest_rate_unsupported_currency_return_400() throws Exception {
        given(getLatestExchangeRateUseCase.getLatestRate(any()))
            .willThrow(new CurrencyNotFoundException("ABC"));

        mockMvc.perform(get("/exchange-rate/latest/ABC"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_CURRENCY"));
    }

    @Test
    @DisplayName("GET /exchange-rate/latest/USD -> 환율 X -> 404 반환")
    void get_latest_rate_not_found_return_404() throws Exception {
        given(getLatestExchangeRateUseCase.getLatestRate(Currency.USD))
            .willThrow(new ExchangeRateNotFoundException(Currency.USD));

        mockMvc.perform(get("/exchange-rate/latest/USD"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("EXCHANGE_RATE_NOT_FOUND"));
    }
}