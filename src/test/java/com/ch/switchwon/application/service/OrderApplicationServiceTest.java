package com.ch.switchwon.application.service;

import com.ch.switchwon.application.port.out.ExchangeRateCachePort;
import com.ch.switchwon.application.port.out.ExchangeRateRepositoryPort;
import com.ch.switchwon.application.port.out.OrderRepositoryPort;
import com.ch.switchwon.domain.exception.ExchangeRateNotFoundException;
import com.ch.switchwon.domain.exception.InvalidOrderRequestException;
import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.domain.model.ExchangeRate;
import com.ch.switchwon.domain.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order application service test")
class OrderApplicationServiceTest {

    @Mock OrderRepositoryPort orderRepositoryPort;
    @Mock ExchangeRateCachePort exchangeRateCachePort;
    @Mock ExchangeRateRepositoryPort exchangeRateRepositoryPort;

    @InjectMocks
    OrderApplicationService service;

    @Nested
    @DisplayName("createOrder - 매수 (KRW -> 외화)")
    class BuyOrderTest {

        @Test
        @DisplayName("KRW -> USD 매수: buyRate * forexAmount = KRW 결제 금액")
        void buy_usd_apply_buy_rate() {
            ExchangeRate rate = ExchangeRate.of(Currency.USD, new BigDecimal("1400.00"));

            given(exchangeRateCachePort.findByCurrency(Currency.USD)).willReturn(Optional.of(rate));
            given(orderRepositoryPort.save(any(Order.class)))
                .willAnswer(inv -> inv.getArgument(0));

            Order result = service.createOrder(Currency.KRW, Currency.USD, new BigDecimal("100"));

            assertThat(result.fromCurrency()).isEqualTo(Currency.KRW);
            assertThat(result.toCurrency()).isEqualTo(Currency.USD);
            assertThat(result.toAmount()).isEqualByComparingTo("100");
            assertThat(result.fromAmount()).isEqualByComparingTo("147000");
            assertThat(result.tradeRate()).isEqualByComparingTo("1470.00");
        }
    }

    @Nested
    @DisplayName("createOrder - 매도 (외화 -> KRW)")
    class SellOrderTest {

        @Test
        @DisplayName("USD -> KRW 매도: sellRate * forexAmount = KRW 수령 금액")
        void sell_usd_apply_sell_rate() {
            ExchangeRate rate = ExchangeRate.of(Currency.USD, new BigDecimal("1400.00"));

            given(exchangeRateCachePort.findByCurrency(Currency.USD)).willReturn(Optional.of(rate));
            given(orderRepositoryPort.save(any(Order.class)))
                .willAnswer(inv -> inv.getArgument(0));

            Order result = service.createOrder(Currency.USD, Currency.KRW, new BigDecimal("100"));

            assertThat(result.fromCurrency()).isEqualTo(Currency.USD);
            assertThat(result.toCurrency()).isEqualTo(Currency.KRW);
            assertThat(result.fromAmount()).isEqualByComparingTo("100");
            assertThat(result.toAmount()).isEqualByComparingTo("133000");
            assertThat(result.tradeRate()).isEqualByComparingTo("1330.00");
        }
    }

    @Nested
    @DisplayName("createOrder - 검증 실패")
    class ValidationTest {

        @Test
        @DisplayName("동일 통화 -> InvalidOrderRequestException")
        void reject_same_currency() {
            BigDecimal amount = new BigDecimal("100");
            assertThatThrownBy(() -> service.createOrder(Currency.USD, Currency.USD, amount))
                .isInstanceOf(InvalidOrderRequestException.class);
        }

        @Test
        @DisplayName("외화 <-> 외화 (KRW 미포함) -> InvalidOrderRequestException")
        void reject_foreign_to_foreign() {
            BigDecimal amount = new BigDecimal("100");
            assertThatThrownBy(() -> service.createOrder(Currency.USD, Currency.JPY, amount))
                .isInstanceOf(InvalidOrderRequestException.class);
        }

        @Test
        @DisplayName("환율 정보 X -> ExchangeRateNotFoundException")
        void throw_when_rate_absent() {
            given(exchangeRateCachePort.findByCurrency(Currency.EUR)).willReturn(Optional.empty());
            given(exchangeRateRepositoryPort.findLatestByCurrency(Currency.EUR)).willReturn(Optional.empty());

            BigDecimal amount = new BigDecimal("100");
            assertThatThrownBy(() -> service.createOrder(Currency.KRW, Currency.EUR, amount))
                .isInstanceOf(ExchangeRateNotFoundException.class);
        }

        @Test
        @DisplayName("0 또는 음수 금액 -> InvalidOrderRequestException")
        void reject_zero_or_negative_amount() {
            ExchangeRate rate = ExchangeRate.of(Currency.USD, new BigDecimal("1400.00"));

            given(exchangeRateCachePort.findByCurrency(Currency.USD)).willReturn(Optional.of(rate));

            assertThatThrownBy(() -> service.createOrder(Currency.KRW, Currency.USD, BigDecimal.ZERO))
                .isInstanceOf(InvalidOrderRequestException.class);
        }
    }
}
