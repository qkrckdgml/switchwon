package com.ch.switchwon.infrastructure.persistence;

import com.ch.switchwon.application.port.out.ExchangeRateRepositoryPort;
import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.domain.model.ExchangeRate;
import com.ch.switchwon.infrastructure.persistence.entity.ExchangeRateEntity;
import com.ch.switchwon.infrastructure.persistence.repository.ExchangeRateJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExchangeRatePersistenceAdapter implements ExchangeRateRepositoryPort {

    private final ExchangeRateJpaRepository jpaRepository;

    @Override
    @Transactional
    public void save(ExchangeRate domain) {
        jpaRepository.save(toEntity(domain));
    }

    @Override
    @Transactional
    public void saveAll(Collection<ExchangeRate> domains) {
        jpaRepository.saveAll(domains.stream().map(this::toEntity).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExchangeRate> findLatestByCurrency(Currency currency) {
        return jpaRepository.findTopByCurrencyOrderByDateTimeDesc(currency)
            .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRate> findAllLatest() {
        return jpaRepository.findAllLatest().stream()
            .map(this::toDomain)
            .toList();
    }

    private ExchangeRateEntity toEntity(ExchangeRate domain) {
        return new ExchangeRateEntity(
            domain.currency(),
            domain.tradeStanRate(),
            domain.buyRate(),
            domain.sellRate(),
            domain.dateTime()
        );
    }

    private ExchangeRate toDomain(ExchangeRateEntity entity) {
        return new ExchangeRate(
            entity.getId(),
            entity.getCurrency(),
            entity.getTradeStanRate().setScale(2, RoundingMode.HALF_UP),
            entity.getBuyRate().setScale(2, RoundingMode.HALF_UP),
            entity.getSellRate().setScale(2, RoundingMode.HALF_UP),
            entity.getDateTime()
        );
    }
}