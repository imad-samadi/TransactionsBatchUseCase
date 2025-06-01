package com.S2M.TransactionsBatchUseCase.Config.Partition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class CurrencyForAggregationPartitioner implements Partitioner {

    private final Set<String> distinctCurrencies;

    public CurrencyForAggregationPartitioner(Set<String> distinctCurrencies) {
        this.distinctCurrencies = distinctCurrencies;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result = new HashMap<>();
        if (distinctCurrencies == null || distinctCurrencies.isEmpty()) {
            log.warn("No distinct currencies for aggregation partitioning.");
            return result;
        }
        int i = 0;
        for (String currency : distinctCurrencies) {
            ExecutionContext context = new ExecutionContext();
            context.putString("aggregationCurrency", currency);
            // Ensure partition names are unique and valid
            result.put("agg_partition_" + sanitizeForPartitionName(currency) + "_" + i++, context);
            log.info("Created aggregation partition for currency: {}", currency);
        }
        return result;
    }

    private String sanitizeForPartitionName(String input) {
        if (input == null) return "null";
        // Replace characters that might be problematic in some contexts (e.g., file systems, JMX MBean names)
        return input.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }
}
