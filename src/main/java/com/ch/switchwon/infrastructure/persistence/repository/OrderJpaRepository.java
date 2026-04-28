package com.ch.switchwon.infrastructure.persistence.repository;

import com.ch.switchwon.infrastructure.persistence.entity.OrderEntity;
import jakarta.persistence.QueryHint;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    @Query("""
        SELECT
            o.id AS id,
            o.fromAmount AS fromAmount,
            o.fromCurrency AS fromCurrency,
            o.toAmount AS toAmount,
            o.toCurrency AS toCurrency,
            o.tradeRate AS tradeRate,
            o.dateTime AS dateTime
        FROM OrderEntity o
        ORDER BY o.dateTime DESC, o.id DESC
    """)
    @QueryHints(value = {
        @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true"),
        @QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "1000")
    })
    List<OrderView> findAllByOrderByDateTimeDesc();
}