package com.S2M.TransactionsBatchUseCase.Config.Partition;

import com.S2M.TransactionsBatchUseCase.DTO.CurrencyInstitutionPair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
@Slf4j
public class CurrencyInstitutionPartitioner implements Partitioner {
    private final Set<CurrencyInstitutionPair> pairsToProcess;

    public CurrencyInstitutionPartitioner(Set<CurrencyInstitutionPair> pairsToProcess) {
        this.pairsToProcess = pairsToProcess;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result = new HashMap<>();
        if (pairsToProcess == null || pairsToProcess.isEmpty()) {
            log.warn("No currency-institution pairs to partition. Step 1 will have no workers.");
            return result;
        }

        int partitionNumber = 0;
        for (CurrencyInstitutionPair pair : pairsToProcess) {
            ExecutionContext context = new ExecutionContext();
            context.putString("processingCurrency", pair.getCurrency());
            context.putString("processingInstitutionId", pair.getInstitutionId());
            String partitionName = "partition_C" + sanitizeForPartitionName(pair.getCurrency()) +
                    "_I" + sanitizeForPartitionName(pair.getInstitutionId()) +
                    "_" + partitionNumber++;
            result.put(partitionName, context);
            log.info("Created partition {} for Currency: {}, Institution: {}",
                    partitionName, pair.getCurrency(), pair.getInstitutionId());
        }
        return result;
    }

    private String sanitizeForPartitionName(String input) {
        if (input == null) return "null";
        return input.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }
}
