package com.ch.switchwon.infrastructure.persistence.repository;

import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.infrastructure.persistence.entity.ExchangeRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExchangeRateJpaRepository extends JpaRepository<ExchangeRateEntity, Long> {

    Optional<ExchangeRateEntity> findTopByCurrencyOrderByDateTimeDesc(Currency currency);

    @Query("""
        SELECT e FROM ExchangeRateEntity e
        WHERE e.id IN (
            SELECT MAX(e2.id) FROM ExchangeRateEntity e2
            GROUP BY e2.currency
        )
    """)
    List<ExchangeRateEntity> findAllLatest();
}