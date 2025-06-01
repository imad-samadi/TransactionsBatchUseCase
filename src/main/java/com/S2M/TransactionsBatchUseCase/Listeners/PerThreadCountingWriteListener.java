package com.S2M.TransactionsBatchUseCase.Listeners;

import com.S2M.TransactionsBatchUseCase.Entity.Repport.SettlementReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
@Component
public class PerThreadCountingWriteListener implements ItemWriteListener<SettlementReport> {

    // Tracks total written items per thread
    private final Map<String, LongAdder> counts = new ConcurrentHashMap<>();


    @Override
    public void afterWrite(Chunk<? extends SettlementReport> items) {
        String thread = Thread.currentThread().getName();

        items.forEach(report -> {
            String institutionId = report.getInstitutionId(); // assuming there's a getInstitutionId() method
            log.info("Thread [{}] wrote SettlementReport for institutionId = {} and currency = {}", thread, institutionId,report.getGlobalReport().getNetSettlementCurren());
        });
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends SettlementReport> items) {
        String thread = Thread.currentThread().getName();
        log.error("Thread [{}] failed to write {} items. Error: {}", thread, items.size(), exception.getMessage());

        items.forEach(report -> {
            String institutionId = report.getInstitutionId();
            log.warn("Failed item had institutionId = {} and currency = {}", institutionId,report.getGlobalReport().getNetSettlementCurren());
        });
    }
}
