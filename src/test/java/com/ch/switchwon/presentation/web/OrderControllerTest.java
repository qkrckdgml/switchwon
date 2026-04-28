package com.ch.switchwon.presentation.web;

import com.ch.switchwon.application.port.in.CreateOrderUseCase;
import com.ch.switchwon.application.port.in.GetOrderListUseCase;
import com.ch.switchwon.config.JacksonConfig;
import com.ch.switchwon.domain.exception.ExchangeRateNotFoundException;
import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.domain.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({GlobalExceptionHandler.class, JacksonConfig.class})
@DisplayName("Order controller test")
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean CreateOrderUseCase createOrderUseCase;
    @MockitoBean GetOrderListUseCase getOrderListUseCase;

    @Nested
    @DisplayName("POST /order")
    class CreateOrderTest {

        @Test
        @DisplayName("KRW -> USD 매수 주문 성공")
        void create_buy_order_return_200() throws Exception {
            Order order = new Order(
                1L,
                new BigDecimal("310264"), Currency.KRW,
                new BigDecimal("200"), Currency.USD,
                new BigDecimal("1551.32"), LocalDateTime.now()
            );

            given(createOrderUseCase.createOrder(Currency.KRW, Currency.USD, new BigDecimal("200")))
                .willReturn(order);

            mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "forexAmount": 200,
                        "fromCurrency": "KRW",
                        "toCurrency": "USD"
                    }
                """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("OK"))
            .andExpect(jsonPath("$.returnObject.id").value(1))
            .andExpect(jsonPath("$.returnObject.fromCurrency").value("KRW"))
            .andExpect(jsonPath("$.returnObject.toCurrency").value("USD"))
            .andExpect(jsonPath("$.returnObject.toAmount").value(200));
        }

        @Test
        @DisplayName("USD -> KRW 매도 주문 시 sellRate 적용")
        void create_sell_order_return_200() throws Exception {
            Order order = new Order(
                2L,
                new BigDecimal("133"), Currency.USD,
                new BigDecimal("186676"), Currency.KRW,
                new BigDecimal("1403.58"), LocalDateTime.now()
            );

            given(createOrderUseCase.createOrder(Currency.USD, Currency.KRW, new BigDecimal("133")))
                .willReturn(order);

            mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "forexAmount": 133,
                        "fromCurrency": "USD",
                        "toCurrency": "KRW"
                    }
                """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.returnObject.fromCurrency").value("USD"))
            .andExpect(jsonPath("$.returnObject.toCurrency").value("KRW"))
            .andExpect(jsonPath("$.returnObject.tradeRate").value(1403.58));
        }

        @Test
        @DisplayName("forexAmount=0 -> 400 반환")
        void create_order_zero_amount_return_400() throws Exception {
            mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "forexAmount": 0,
                        "fromCurrency": "KRW",
                        "toCurrency": "USD"
                    }
                """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("forexAmount 누락 -> 400 반환")
        void create_order_missing_amount_return_400() throws Exception {
            mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "fromCurrency": "KRW",
                        "toCurrency": "USD"
                    }
                """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("잘못된 Json 요청 -> 400 INVALID_REQUEST_BODY 반환")
        void create_order_invalid_request_return_400() throws Exception {
            mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
        }

        @Test
        @DisplayName("환율 정보 X -> 404 EXCHANGE_RATE_NOT_FOUND 반환")
        void create_order_no_exchange_rate_return_404() throws Exception {
            given(createOrderUseCase.createOrder(any(), any(), any()))
                .willThrow(new ExchangeRateNotFoundException(Currency.EUR));

            mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "forexAmount": 100,
                        "fromCurrency": "KRW",
                        "toCurrency": "EUR"
                    }
                """)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("EXCHANGE_RATE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("GET /order/list")
    class GetOrderListTest {

        @Test
        @DisplayName("주문 목록 -> 200 반환")
        void get_orders_return_200() throws Exception {
            List<Order> orders = List.of(
                new Order(
                    1L,
                    new BigDecimal("310264"), Currency.KRW,
                    new BigDecimal("200"), Currency.USD,
                    new BigDecimal("1551.32"), LocalDateTime.now()
                ),
                new Order(
                    2L,
                    new BigDecimal("133"), Currency.USD,
                    new BigDecimal("186676"), Currency.KRW,
                    new BigDecimal("1403.58"), LocalDateTime.now()
                )
            );

            given(getOrderListUseCase.getOrders()).willReturn(orders);

            mockMvc.perform(get("/order/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.returnObject.orderList.length()").value(2))
                .andExpect(jsonPath("$.returnObject.orderList[0].id").value(1))
                .andExpect(jsonPath("$.returnObject.orderList[1].id").value(2));
        }

        @Test
        @DisplayName("주문 목록 Empty -> 빈 목록 반환")
        void get_orders_empty_return_200() throws Exception {
            given(getOrderListUseCase.getOrders()).willReturn(List.of());

            mockMvc.perform(get("/order/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnObject.orderList").isEmpty());
        }
    }
}