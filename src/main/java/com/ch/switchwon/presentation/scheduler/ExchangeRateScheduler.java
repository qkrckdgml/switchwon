package com.ch.switchwon.presentation.scheduler;

import com.ch.switchwon.application.port.in.RefreshExchangeRatesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

    private final RefreshExchangeRatesUseCase refreshExchangeRatesUseCase;

    private final AtomicBoolean refreshing = new AtomicBoolean(false);

    @Scheduled(fixedRate = 60_000, initialDelay = 0)
    public void scheduleExchangeRateRefresh() {
        if (!refreshing.compareAndSet(false, true)) {
            log.warn("[스케줄러] 이전 환율 갱신 실행 중 - skip");
            return;
        }

        log.info("[스케줄러] 환율 갱신 시작 - thread: {}", Thread.currentThread());
        try {
            refreshExchangeRatesUseCase.refresh();
        } catch (Exception e) {
            log.error("[스케줄러] 환율 갱신 실패", e);
        } finally {
            refreshing.set(false);
        }
    }
}