package com.ch.switchwon.integration;

import com.ch.switchwon.application.port.out.ExchangeRateApiPort;
import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.domain.model.ExchangeRate;
import com.ch.switchwon.infrastructure.cache.ExchangeRateCacheAdapter;
import com.ch.switchwon.infrastructure.persistence.entity.ExchangeRateEntity;
import com.ch.switchwon.infrastructure.persistence.repository.ExchangeRateJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ExchangeRate API integration test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExchangeRateIntegrationTest {

    @MockitoBean
    ExchangeRateApiPort exchangeRateApiPort;

    @Autowired MockMvc mockMvc;
    @Autowired ExchangeRateJpaRepository exchangeRateJpaRepository;
    @Autowired ExchangeRateCacheAdapter exchangeRateCacheAdapter;

    private static final BigDecimal USD_BASE = new BigDecimal("1400.00");
    private static final BigDecimal JPY_BASE = new BigDecimal("900.00");
    private static final BigDecimal CNY_BASE = new BigDecimal("190.00");
    private static final BigDecimal EUR_BASE = new BigDecimal("1500.00");

    @BeforeEach
    void setUp() {
        given(exchangeRateApiPort.fetchBaseRates()).willReturn(Map.of());

        exchangeRateJpaRepository.deleteAll();
        exchangeRateCacheAdapter.putAll(Map.of());

        seedAllCurrencies();
    }

    @Test
    @Order(1)
    @DisplayName("전체 환율 조회 - Cache Hit -> DB 조회 없이 즉시 응답")
    void get_latest_rate_cache_hit_return_200() throws Exception {
        mockMvc.perform(get("/exchange-rate/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("OK"))
            .andExpect(jsonPath("$.returnObject.exchangeRateList").isArray())
            .andExpect(jsonPath("$.returnObject.exchangeRateList.length()").value(4));
    }

    @Test
    @Order(2)
    @DisplayName("전체 환율 조회 - Cache Miss -> DB Fallback 조회")
    void get_latest_rate_cache_miss_fallback() throws Exception {
        exchangeRateCacheAdapter.putAll(Map.of());

        mockMvc.perform(get("/exchange-rate/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.returnObject.exchangeRateList.length()").value(4));
    }

    @Test
    @Order(3)
    @DisplayName("전체 환율 조회 - Cache/DB 모두 비어있으면 빈 배열 반환")
    void get_latest_rate_empty_return_empty() throws Exception {
        exchangeRateJpaRepository.deleteAll();
        exchangeRateCacheAdapter.putAll(Map.of());

        mockMvc.perform(get("/exchange-rate/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.returnObject.exchangeRateList").isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("USD 환율 조회 -> buyRate = tradeStanRate * 1.05, sellRate = * 0.95")
    void get_latest_rate_usd_correct() throws Exception {
        mockMvc.perform(get("/exchange-rate/latest/USD"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("OK"))
            .andExpect(jsonPath("$.returnObject.currency").value("USD"))
            .andExpect(jsonPath("$.returnObject.tradeStanRate").value(1400.00))
            .andExpect(jsonPath("$.returnObject.buyRate").value(1470.00))
            .andExpect(jsonPath("$.returnObject.sellRate").value(1330.00));
    }

    @Test
    @Order(5)
    @DisplayName("소문자 통화 코드 -> 정상 조회")
    void get_latest_rate_lowercase_currency_success() throws Exception {
        mockMvc.perform(get("/exchange-rate/latest/jpy"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.returnObject.currency").value("JPY"));
    }

    @Test
    @Order(6)
    @DisplayName("지원하지 않는 통화 -> 400 INVALID_CURRENCY")
    void get_latest_rate_unsupported_currency_return_400() throws Exception {
        mockMvc.perform(get("/exchange-rate/latest/ABC"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_CURRENCY"));
    }

    @Test
    @Order(7)
    @DisplayName("환율 데이터 X -> 통화 조회 -> 404 EXCHANGE_RATE_NOT_FOUND")
    void get_latest_rate_no_data_return_404() throws Exception {
        exchangeRateJpaRepository.deleteAll();
        exchangeRateCacheAdapter.putAll(Map.of());

        mockMvc.perform(get("/exchange-rate/latest/USD"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("EXCHANGE_RATE_NOT_FOUND"));
    }

    @Test
    @Order(8)
    @DisplayName("환율 이력 저장 체크")
    void exchange_rate_persisted_in_database() {
        assertThat(exchangeRateJpaRepository.count()).isEqualTo(4);
        assertThat(exchangeRateJpaRepository.findTopByCurrencyOrderByDateTimeDesc(Currency.USD))
            .isPresent()
            .hasValueSatisfying(e -> {
                assertThat(e.getCurrency()).isEqualTo(Currency.USD);
                assertThat(e.getTradeStanRate()).isEqualByComparingTo("1400.00");
                assertThat(e.getBuyRate()).isEqualByComparingTo("1470.00");
                assertThat(e.getSellRate()).isEqualByComparingTo("1330.00");
            });
    }

    private void seedAllCurrencies() {
        Map<Currency, BigDecimal> bases = new EnumMap<>(Currency.class);
        bases.put(Currency.USD, USD_BASE);
        bases.put(Currency.JPY, JPY_BASE);
        bases.put(Currency.CNY, CNY_BASE);
        bases.put(Currency.EUR, EUR_BASE);

        LocalDateTime now = LocalDateTime.now();
        Map<Currency, ExchangeRate> cacheMap = new EnumMap<>(Currency.class);

        bases.forEach((currency, base) -> {
            BigDecimal stan = base.setScale(2, RoundingMode.HALF_UP);
            BigDecimal buy  = base.multiply(new BigDecimal("1.05")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal sell = base.multiply(new BigDecimal("0.95")).setScale(2, RoundingMode.HALF_UP);

            exchangeRateJpaRepository.save(new ExchangeRateEntity(currency, stan, buy, sell, now));
            cacheMap.put(currency, new ExchangeRate(null, currency, stan, buy, sell, now));
        });

        exchangeRateCacheAdapter.putAll(cacheMap);
    }
}