package com.S2M.TransactionsBatchUseCase.Listeners;

import com.S2M.TransactionsBatchUseCase.Entity.Repport.WalletActivityReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WalletActivityReportWriteListener implements ItemWriteListener<WalletActivityReport> {

    // Total count written per thread (optional)
    private final Map<String, LongAdder> counts = new ConcurrentHashMap<>();

    @Override
    public void afterWrite(Chunk<? extends WalletActivityReport> items) {
        String thread = Thread.currentThread().getName();
        LongAdder adder = counts.computeIfAbsent(thread, t -> new LongAdder());
        adder.add(items.size());
        long total = adder.sum();

        for (WalletActivityReport report : items) {
            log.info("Thread [{}] wrote WalletActivityReport for currency [{}]", thread, report.getActivityReportCurrency());
        }

        log.debug("Thread [{}] wrote {} WalletActivityReports in this chunk, cumulative total = {}",
                thread, items.size(), total);
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends WalletActivityReport> items) {
        String thread = Thread.currentThread().getName();
        LongAdder adder = counts.get(thread);
        long total = (adder != null) ? adder.sum() : 0;

        for (WalletActivityReport report : items) {
            log.error("Thread [{}] failed to write WalletActivityReport for currency [{}]. Error: {}",
                    thread, report.getActivityReportCurrency(), exception.getMessage());
        }

        log.error("Thread [{}] failed to write {} WalletActivityReports in this chunk (cumulative = {}). Error: {}",
                thread, items.size(), total, exception.getMessage());
    }
}
