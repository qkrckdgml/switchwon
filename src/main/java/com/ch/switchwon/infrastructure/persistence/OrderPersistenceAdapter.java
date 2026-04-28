package com.ch.switchwon.infrastructure.persistence;

import com.ch.switchwon.application.port.out.OrderRepositoryPort;
import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.domain.model.Order;
import com.ch.switchwon.infrastructure.persistence.entity.OrderEntity;
import com.ch.switchwon.infrastructure.persistence.repository.OrderJpaRepository;
import com.ch.switchwon.infrastructure.persistence.repository.OrderView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository jpaRepository;

    @Override
    @Transactional
    public Order save(Order domain) {
        return toDomain(jpaRepository.save(toEntity(domain)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return jpaRepository.findAllByOrderByDateTimeDesc().stream()
            .map(this::toDomain)
            .toList();
    }

    private Order toDomain(OrderEntity entity) {
        return new Order(
            entity.getId(),
            normalize(entity.getFromAmount(), entity.getFromCurrency()),
            entity.getFromCurrency(),
            normalize(entity.getToAmount(), entity.getToCurrency()),
            entity.getToCurrency(),
            entity.getTradeRate().setScale(2, RoundingMode.HALF_UP),
            entity.getDateTime()
        );
    }

    private Order toDomain(OrderView model) {
        return new Order(
            model.getId(),
            normalize(model.getFromAmount(), model.getFromCurrency()),
            model.getFromCurrency(),
            normalize(model.getToAmount(), model.getToCurrency()),
            model.getToCurrency(),
            model.getTradeRate().setScale(2, RoundingMode.HALF_UP),
            model.getDateTime()
        );
    }

    private OrderEntity toEntity(Order domain) {
        return new OrderEntity(
            domain.fromAmount(),
            domain.fromCurrency(),
            domain.toAmount(),
            domain.toCurrency(),
            domain.tradeRate(),
            domain.dateTime()
        );
    }

    private static BigDecimal normalize(BigDecimal amount, Currency currency) {
        return currency == Currency.KRW
            ? amount.setScale(0, RoundingMode.UNNECESSARY)
            : amount.setScale(2, RoundingMode.HALF_UP);
    }
}