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
@Table(name = "exchange_rate_history",
   indexes = @Index(name = "idx_currency_datetime", columnList = "currency, dateTime DESC"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeRateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal tradeStanRate;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal buyRate;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal sellRate;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    public ExchangeRateEntity(
        Currency currency,
        BigDecimal tradeStanRate,
        BigDecimal buyRate,
        BigDecimal sellRate,
        LocalDateTime dateTime
    ) {
        this.currency = currency;
        this.tradeStanRate = tradeStanRate;
        this.buyRate = buyRate;
        this.sellRate = sellRate;
        this.dateTime = dateTime;
    }
}
