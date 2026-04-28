package com.ch.switchwon.infrastructure.persistence.entity;

import com.ch.switchwon.domain.model.Currency;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "forex_order",
   indexes = @Index(name = "idx_order_datetime", columnList = "dateTime DESC"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 20, scale = 4)
    private BigDecimal fromAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency fromCurrency;

    @Column(nullable = false, precision = 20, scale = 4)
    private BigDecimal toAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency toCurrency;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal tradeRate;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    public OrderEntity(
        BigDecimal fromAmount,
        Currency fromCurrency,
        BigDecimal toAmount,
        Currency toCurrency,
        BigDecimal tradeRate,
        LocalDateTime dateTime
    ) {
        this.fromAmount = fromAmount;
        this.fromCurrency = fromCurrency;
        this.toAmount = toAmount;
        this.toCurrency = toCurrency;
        this.tradeRate = tradeRate;
        this.dateTime = dateTime;
    }
}
