package com.ch.switchwon.application.service;

import com.ch.switchwon.application.port.out.ExchangeRateApiPort;
import com.ch.switchwon.application.port.out.ExchangeRateCachePort;
import com.ch.switchwon.application.port.out.ExchangeRateRepositoryPort;
import com.ch.switchwon.domain.exception.ExchangeRateNotFoundException;
import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.domain.model.ExchangeRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Exchange rate application service test")
class ExchangeRateApplicationServiceTest {

    @Mock ExchangeRateApiPort exchangeRateApiPort;
    @Mock ExchangeRateCachePort exchangeRateCachePort;
    @Mock ExchangeRateRepositoryPort exchangeRateRepositoryPort;

    @InjectMocks
    ExchangeRateApplicationService service;

    @Nested
    @DisplayName("getLatestRates")
    class GetLatestRatesTest {

        @Test
        @DisplayName("Cache Not Empty -> 캐시 조회")
        void return_cache_when_cache_not_empty() {
            List<ExchangeRate> cached = List.of(
                ExchangeRate.of(Currency.USD, new BigDecimal("1400.00"))
            );

            given(exchangeRateCachePort.findAll()).willReturn(cached);

            List<ExchangeRate> result = service.getLatestRates();

            assertThat(result).isEqualTo(cached);
            then(exchangeRateRepositoryPort).should(never()).findAllLatest();
        }

        @Test
        @DisplayName("Cache Empty -> DB 조회")
        void return_database_when_cache_empty() {
            List<ExchangeRate> dbRates = List.of(
                ExchangeRate.of(Currency.JPY, new BigDecimal("900.00"))
            );

            given(exchangeRateCachePort.findAll()).willReturn(List.of());
            given(exchangeRateRepositoryPort.findAllLatest()).willReturn(dbRates);

            List<ExchangeRate> result = service.getLatestRates();

            assertThat(result).isEqualTo(dbRates);
        }
    }

    @Nested
    @DisplayName("getLatestRate")
    class GetLatestRateTest {

        @Test
        @DisplayName("Cache Hit")
        void return_cache_when_present() {
            ExchangeRate cached = ExchangeRate.of(Currency.USD, new BigDecimal("1400.00"));

            given(exchangeRateCachePort.findByCurrency(Currency.USD)).willReturn(Optional.of(cached));

            ExchangeRate result = service.getLatestRate(Currency.USD);

            assertThat(result).isEqualTo(cached);
            then(exchangeRateRepositoryPort).should(never()).findLatestByCurrency(any());
        }

        @Test
        @DisplayName("Cache Miss -> DB 조회")
        void return_database_when_cache_miss() {
            ExchangeRate dbRate = ExchangeRate.of(Currency.EUR, new BigDecimal("1500.00"));

            given(exchangeRateCachePort.findByCurrency(Currency.EUR)).willReturn(Optional.empty());
            given(exchangeRateRepositoryPort.findLatestByCurrency(Currency.EUR)).willReturn(Optional.of(dbRate));

            ExchangeRate result = service.getLatestRate(Currency.EUR);

            assertThat(result).isEqualTo(dbRate);
        }

        @Test
        @DisplayName("Cache / DB Empty -> ExchangeRateNotFoundException 예외")
        void throw_when_all_absent() {
            given(exchangeRateCachePort.findByCurrency(Currency.CNY)).willReturn(Optional.empty());
            given(exchangeRateRepositoryPort.findLatestByCurrency(Currency.CNY)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.getLatestRate(Currency.CNY))
                    .isInstanceOf(ExchangeRateNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("refresh")
    class RefreshTest {

        @Test
        @DisplayName("외부 API 응답 Empty -> 갱신 skip")
        void skip_when_return_empty() {
            given(exchangeRateApiPort.fetchBaseRates()).willReturn(Map.of());

            service.refresh();

            then(exchangeRateRepositoryPort).should(never()).saveAll(any());
            then(exchangeRateCachePort).should(never()).putAll(any());
        }

        @Test
        @DisplayName("외부 API 응답 Ok -> saveAll()로 일괄 저장 -> Cache 갱신")
        void save_all_currencies_and_updates_cache() {
            Map<Currency, BigDecimal> base = Map.of(
                Currency.USD, new BigDecimal("1400.00"),
                Currency.JPY, new BigDecimal("900.00"),
                Currency.CNY, new BigDecimal("190.00"),
                Currency.EUR, new BigDecimal("1500.00")
            );

            given(exchangeRateApiPort.fetchBaseRates()).willReturn(base);

            service.refresh();

            then(exchangeRateRepositoryPort).should(times(1)).saveAll(any());
            then(exchangeRateCachePort).should().putAll(any());
        }
    }
}
