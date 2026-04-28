package com.ch.switchwon.infrastructure.external;

import com.ch.switchwon.application.port.out.ExchangeRateApiPort;
import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.infrastructure.external.dto.OpenExchangeRateApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenExchangeRateAdapter implements ExchangeRateApiPort {

    private static final Map<Currency, double[]> FALLBACK_RANGES = Map.of(
        Currency.USD, new double[]{1300.0, 1500.0},
        Currency.JPY, new double[]{850.0, 1050.0},
        Currency.CNY, new double[]{170.0, 210.0},
        Currency.EUR, new double[]{1350.0, 1600.0}
    );
    private static final double RANDOM_VARIATION = 0.003;
    private final RestClient restClient;

    @Override
    public Map<Currency, BigDecimal> fetchBaseRates() {
        try {
            OpenExchangeRateApiResponse response = restClient.get()
                .retrieve()
                .body(OpenExchangeRateApiResponse.class);

            if (response == null || !response.isSuccess() || response.rates() == null) {
                log.warn("[환율 API] 응답 Fail -> Fallback");
                return generateFallbackRates();
            }

            return convertToKrwBaseRates(response.rates());

        } catch (RestClientException e) {
            log.warn("[환율 API] 호출 Fail ({}) -> Fallback", e.getMessage());
            return generateFallbackRates();
        }
    }

    private Map<Currency, BigDecimal> convertToKrwBaseRates(Map<String, BigDecimal> rates) {
        BigDecimal krwPerUsd = rates.getOrDefault("KRW", BigDecimal.ZERO);
        if (krwPerUsd.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("[환율 API] KRW 환율 누락 -> Fallback");
            return generateFallbackRates();
        }

        Map<Currency, BigDecimal> result = new EnumMap<>(Currency.class);

        result.put(Currency.USD, applyRandomVariation(krwPerUsd));

        BigDecimal jpyPerUsd = rates.getOrDefault("JPY", BigDecimal.ONE);
        BigDecimal jpy100Rate = krwPerUsd
            .divide(jpyPerUsd, 10, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        result.put(Currency.JPY, applyRandomVariation(jpy100Rate));

        BigDecimal cnyPerUsd = rates.getOrDefault("CNY", BigDecimal.ONE);
        result.put(Currency.CNY, applyRandomVariation(krwPerUsd.divide(cnyPerUsd, 10, RoundingMode.HALF_UP)));

        BigDecimal eurPerUsd = rates.getOrDefault("EUR", BigDecimal.ONE);
        result.put(Currency.EUR, applyRandomVariation(krwPerUsd.divide(eurPerUsd, 10, RoundingMode.HALF_UP)));

        return result;
    }

    private BigDecimal applyRandomVariation(BigDecimal rate) {
        double variation = 1.0 + (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * RANDOM_VARIATION;

        return rate.multiply(BigDecimal.valueOf(variation));
    }

    private Map<Currency, BigDecimal> generateFallbackRates() {
        Map<Currency, BigDecimal> fallback = new EnumMap<>(Currency.class);
        FALLBACK_RANGES.forEach((currency, range) -> {
            double randomRate = range[0] + ThreadLocalRandom.current().nextDouble() * (range[1] - range[0]);
            fallback.put(currency, BigDecimal.valueOf(randomRate));
        });

        return fallback;
    }
}