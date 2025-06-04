package com.S2M.TransactionsBatchUseCase.Reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.IteratorItemReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
public class DistinctCurrencyReader implements ItemReader<String> {

    private final IteratorItemReader<String> delegateReader;

    /**
     * Constructor that takes the pre-fetched set of distinct currencies.
     * @param distinctCurrencies Set of currency codes from JobExecutionContext.
     */
    public DistinctCurrencyReader(Set<String> distinctCurrencies) {
        if (distinctCurrencies == null || distinctCurrencies.isEmpty()) {
            log.info("DistinctCurrencyReader: Initialized with no distinct currencies.");
            this.delegateReader = new IteratorItemReader<>(Collections.emptyList());
        } else {


            List<String> sortedCurrencies = new ArrayList<>(distinctCurrencies);
            Collections.sort(sortedCurrencies); // Optional: Sort for consistent processing order
            log.info("DistinctCurrencyReader: Initialized with distinct currencies: {}", sortedCurrencies);
            this.delegateReader = new IteratorItemReader<>(sortedCurrencies);
        }
    }

    @Override
    public String read() throws Exception {
        // Delegate the actual read operation to the IteratorItemReader
        String currency = delegateReader.read();
        if (currency != null) {
            log.debug("DistinctCurrencyReader: Emitting currency - {}", currency);
        } else {
            log.debug("DistinctCurrencyReader: No more currencies to emit.");
        }
        return currency;
    }
}
