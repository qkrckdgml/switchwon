package com.ch.switchwon.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExchangeRate domain test")
class ExchangeRateTest {

    @Test
    @DisplayName("of() - buyRate = tradeStanRate * 1.05, sellRate = tradeStanRate * 0.95 (소수 둘째 자리 반올림)")
    void of_calculate_buy_and_sell_rate() {
        ExchangeRate rate = ExchangeRate.of(Currency.USD, new BigDecimal("1400.00"));

        assertThat(rate.currency()).isEqualTo(Currency.USD);
        assertThat(rate.tradeStanRate()).isEqualByComparingTo("1400.00");
        assertThat(rate.buyRate()).isEqualByComparingTo("1470.00");
        assertThat(rate.sellRate()).isEqualByComparingTo("1330.00");
        assertThat(rate.id()).isNull();
        assertThat(rate.dateTime()).isNotNull();
    }

    @Test
    @DisplayName("of() - 소수점 환율 -> 둘째 자리 반올림")
    void of_round_half_up() {
        ExchangeRate rate = ExchangeRate.of(Currency.JPY, new BigDecimal("923.7777"));

        assertThat(rate.tradeStanRate()).isEqualByComparingTo("923.78");
        assertThat(rate.buyRate()).isEqualByComparingTo("969.97");
        assertThat(rate.sellRate()).isEqualByComparingTo("877.59");
    }

    @Test
    @DisplayName("of() - 모든 지원 통화에 대해 정상 동작")
    void of_work_all_supported_currencies() {
        for (Currency c : Currency.SUPPORTED_FOREIGN) {
            ExchangeRate rate = ExchangeRate.of(c, new BigDecimal("1000.00"));
            assertThat(rate.buyRate()).isEqualByComparingTo("1050.00");
            assertThat(rate.sellRate()).isEqualByComparingTo("950.00");
        }
    }
}
