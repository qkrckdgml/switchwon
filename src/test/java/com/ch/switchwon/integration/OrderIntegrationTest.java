package com.ch.switchwon.integration;

import com.ch.switchwon.application.port.out.ExchangeRateApiPort;
import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.domain.model.ExchangeRate;
import com.ch.switchwon.infrastructure.cache.ExchangeRateCacheAdapter;
import com.ch.switchwon.infrastructure.persistence.entity.ExchangeRateEntity;
import com.ch.switchwon.infrastructure.persistence.entity.OrderEntity;
import com.ch.switchwon.infrastructure.persistence.repository.ExchangeRateJpaRepository;
import com.ch.switchwon.infrastructure.persistence.repository.OrderJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Order API integration test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderIntegrationTest {

    @MockitoBean ExchangeRateApiPort exchangeRateApiPort;
    @Autowired MockMvc mockMvc;
    @Autowired OrderJpaRepository orderJpaRepository;
    @Autowired ExchangeRateJpaRepository exchangeRateJpaRepository;
    @Autowired ExchangeRateCacheAdapter exchangeRateCacheAdapter;

    @BeforeEach
    void setUp() {
        given(exchangeRateApiPort.fetchBaseRates()).willReturn(Map.of());
        orderJpaRepository.deleteAll();
        exchangeRateJpaRepository.deleteAll();
        exchangeRateCacheAdapter.putAll(Map.of());
        seedExchangeRates();
    }

    @Test
    @Order(1)
    @DisplayName("매수 주문 성공 (KRW -> USD): buyRate 적용, 저장 확인")
    void create_buy_order_success() throws Exception {
        mockMvc.perform(post("/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"forexAmount": 200, "fromCurrency": "KRW", "toCurrency": "USD"}
            """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("OK"))
        .andExpect(jsonPath("$.returnObject.fromCurrency").value("KRW"))
        .andExpect(jsonPath("$.returnObject.toCurrency").value("USD"))
        .andExpect(jsonPath("$.returnObject.toAmount").value(200.0))
        .andExpect(jsonPath("$.returnObject.fromAmount").value(294000))
        .andExpect(jsonPath("$.returnObject.tradeRate").value(1470.00))
        .andExpect(jsonPath("$.returnObject.id").isNumber());

        assertThat(orderJpaRepository.count()).isEqualTo(1);
        orderJpaRepository.findAll().stream()
            .findFirst()
            .ifPresent(entity -> {
                assertThat(entity.getFromCurrency()).isEqualTo(Currency.KRW);
                assertThat(entity.getToCurrency()).isEqualTo(Currency.USD);
                assertThat(entity.getFromAmount().setScale(0, RoundingMode.DOWN))
                    .isEqualByComparingTo("294000");
            });
    }

    @Test
    @Order(2)
    @DisplayName("매도 주문 성공 (USD -> KRW): sellRate 적용")
    void create_sell_order_success() throws Exception {
        mockMvc.perform(post("/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"forexAmount": 133, "fromCurrency": "USD", "toCurrency": "KRW"}
            """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.returnObject.fromCurrency").value("USD"))
        .andExpect(jsonPath("$.returnObject.toCurrency").value("KRW"))
        .andExpect(jsonPath("$.returnObject.tradeRate").value(1330.00));

        assertThat(orderJpaRepository.count()).isEqualTo(1);
    }

    @Test
    @Order(3)
    @DisplayName("원화 Floor 처리 확인")
    void create_order_floor_truncate_decimal() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        exchangeRateJpaRepository.save(
            new ExchangeRateEntity(
                Currency.CNY,
                new BigDecimal("190.00"),
                new BigDecimal("199.50"),
                new BigDecimal("180.50"),
                now
            )
        );

        mockMvc.perform(post("/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"forexAmount": 1, "fromCurrency": "CNY", "toCurrency": "KRW"}
            """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.returnObject.toAmount").value(180));
    }

    @Test
    @Order(4)
    @DisplayName("JPY 매수 -> 100엔 기준 환율 적용 확인")
    void create_buy_order_jpy_100_unit() throws Exception {
        mockMvc.perform(post("/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"forexAmount": 200, "fromCurrency": "KRW", "toCurrency": "JPY"}
            """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.returnObject.toCurrency").value("JPY"))
        .andExpect(jsonPath("$.returnObject.fromAmount").value(189000));
    }

    @Test
    @Order(5)
    @DisplayName("forexAmount=0 -> 400 VALIDATION_ERROR")
    void create_order_zero_amount_return_400() throws Exception {
        mockMvc.perform(post("/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"forexAmount": 0, "fromCurrency": "KRW", "toCurrency": "USD"}
            """)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @Order(6)
    @DisplayName("fromCurrency 누락 -> 400 VALIDATION_ERROR")
    void create_order_missing_fromCurrency_return_400() throws Exception {
        mockMvc.perform(post("/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"forexAmount": 100, "toCurrency": "USD"}
            """)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @Order(7)
    @DisplayName("동일 통화 (KRW -> KRW) -> 400 INVALID_ORDER_REQUEST")
    void create_order_same_currency_return_400() throws Exception {
        mockMvc.perform(post("/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"forexAmount": 100, "fromCurrency": "KRW", "toCurrency": "KRW"}
            """)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_ORDER_REQUEST"));
    }

    @Test
    @Order(8)
    @DisplayName("외화 <-> 외화 (KRW 미포함) -> 400 INVALID_ORDER_REQUEST")
    void create_order_foreign_to_foreign_return_400() throws Exception {
        mockMvc.perform(post("/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"forexAmount": 100, "fromCurrency": "USD", "toCurrency": "EUR"}
            """)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_ORDER_REQUEST"));
    }

    @Test
    @Order(9)
    @DisplayName("지원하지 않는 통화(XYZ) -> 400 INVALID_CURRENCY")
    void create_order_unsupported_currency_return_400() throws Exception {
        mockMvc.perform(post("/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"forexAmount": 100, "fromCurrency": "KRW", "toCurrency": "XYZ"}
            """)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_CURRENCY"));
    }

    @Test
    @Order(10)
    @DisplayName("환율 정보 X -> 주문 - 404 EXCHANGE_RATE_NOT_FOUND")
    void create_order_no_exchange_rate_return_404() throws Exception {
        exchangeRateJpaRepository.deleteAll();
        exchangeRateCacheAdapter.putAll(Map.of());

        mockMvc.perform(post("/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"forexAmount": 100, "fromCurrency": "KRW", "toCurrency": "USD"}
            """)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("EXCHANGE_RATE_NOT_FOUND"));
    }

    @Test
    @Order(11)
    @DisplayName("주문 목록 조회 - 생성 후 최신순 반환 확인")
    void get_orders_latest() throws Exception {
        mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"forexAmount": 100, "fromCurrency": "KRW", "toCurrency": "USD"}
            """)
        );

        mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"forexAmount": 50, "fromCurrency": "USD", "toCurrency": "KRW"}
            """)
        );

        mockMvc.perform(get("/order/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.returnObject.orderList.length()").value(2))
            .andExpect(jsonPath("$.returnObject.orderList[0].fromCurrency").value("USD"))
            .andExpect(jsonPath("$.returnObject.orderList[1].fromCurrency").value("KRW"));
    }

    @Test
    @Order(12)
    @DisplayName("동시 주문 20건 - 성공, DB 정합성 체크")
    void concurrent_order_20_threads() throws InterruptedException {
        int threadCount = 20;
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startGate.await();
                        mockMvc.perform(post("/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {"forexAmount": 1, "fromCurrency": "KRW", "toCurrency": "USD"}
                            """)
                        )
                        .andExpect(status().isOk());

                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                        log.error("동시 주문 실패: {}", e.getMessage());
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startGate.countDown();
        }

        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);

        assertThat(completed).as("30초 내 완료").isTrue();
        assertThat(failCount.get()).as("실패 0건").isZero();
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(orderJpaRepository.count()).isEqualTo((long) threadCount);

        log.info("동시 주문 결과 - 성공: {}, 실패: {}, DB 저장: {}",
            successCount.get(), failCount.get(), orderJpaRepository.count());
    }

    @Test
    @Order(13)
    @DisplayName("전체 조회 확인")
    void order_query_all() throws Exception {
        int dataSize = 3_000;
        LocalDateTime base = LocalDateTime.now().minusMinutes(dataSize);

        List<OrderEntity> bulk = IntStream.range(0, dataSize)
            .mapToObj(i -> new OrderEntity(
                new BigDecimal("294000.0000"),
                Currency.KRW,
                new BigDecimal("200.0000"),
                Currency.USD,
                new BigDecimal("1470.0000"),
                base.plusSeconds(i)
            ))
            .toList();

        orderJpaRepository.saveAll(bulk);

        assertThat(orderJpaRepository.count()).isEqualTo(dataSize);

        long start = System.currentTimeMillis();
        mockMvc.perform(get("/order/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.returnObject.orderList.length()").value(dataSize));
        long elapsed = System.currentTimeMillis() - start;

        log.info("데이터 {}건 조회: {}ms", dataSize, elapsed);
        assertThat(elapsed).isLessThan(3_000);
    }

    private void seedExchangeRates() {
        Map<Currency, BigDecimal> bases = new EnumMap<>(Currency.class);
        bases.put(Currency.USD, new BigDecimal("1400.00"));
        bases.put(Currency.JPY, new BigDecimal("900.00"));
        bases.put(Currency.CNY, new BigDecimal("190.00"));
        bases.put(Currency.EUR, new BigDecimal("1500.00"));

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