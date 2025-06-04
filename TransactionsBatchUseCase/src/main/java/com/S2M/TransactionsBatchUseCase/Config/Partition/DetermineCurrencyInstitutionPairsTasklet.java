    package com.S2M.TransactionsBatchUseCase.Config.Partition;

    import com.S2M.TransactionsBatchUseCase.DTO.CurrencyInstitutionPair;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.batch.core.StepContribution;
    import org.springframework.batch.core.scope.context.ChunkContext;
    import org.springframework.batch.core.step.tasklet.Tasklet;
    import org.springframework.batch.repeat.RepeatStatus;
    import org.springframework.jdbc.core.JdbcTemplate;

    import javax.sql.DataSource;
    import java.util.HashSet;
    import java.util.List;
    import java.util.Set;
    @Slf4j

    public class DetermineCurrencyInstitutionPairsTasklet implements Tasklet {
        public static final String CURRENCY_INSTITUTION_PAIRS_KEY = "currencyInstitutionPairs";
        public static final String DISTINCT_CURRENCIES_KEY = "distinctCurrencies";

        private final JdbcTemplate jdbcTemplate;
        private final String sessionId;
        private final String centerId;

        public DetermineCurrencyInstitutionPairsTasklet(DataSource dataSource, String sessionId, String centerId) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
            this.sessionId = sessionId;
            this.centerId = centerId;
        }

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
            log.info("Determining work units for sessionId: {}, centerId: {}", sessionId, centerId);


            String distinctCurrenciesSql = "SELECT DISTINCT TX_CURRENCY FROM TRANSACTION " +
                    "WHERE SESSION_ID = ? AND TX_CENTER_ID = ?";
            List<String> distinctCurrencies = jdbcTemplate.queryForList(distinctCurrenciesSql, String.class, sessionId, centerId);

            if (distinctCurrencies.isEmpty()) {
                log.warn("No distinct currencies found for sessionId: {}, centerId: {}. No reports will be generated.", sessionId, centerId);
                contribution.getStepExecution().getJobExecution().getExecutionContext()
                        .put(CURRENCY_INSTITUTION_PAIRS_KEY, new HashSet<>());
                contribution.getStepExecution().getJobExecution().getExecutionContext()
                        .put(DISTINCT_CURRENCIES_KEY, new HashSet<>());
                return RepeatStatus.FINISHED;
            }
            log.info("Found distinct currencies: {}", distinctCurrencies);

            Set<CurrencyInstitutionPair> pairs = new HashSet<>();

            String institutionsSql = "SELECT DISTINCT DEBIT_INST_REF FROM TRANSACTION " +
                    "WHERE SESSION_ID = ? AND TX_CENTER_ID = ? AND TX_CURRENCY = ? AND DEBIT_INST_REF IS NOT NULL " +
                    "UNION " +
                    "SELECT DISTINCT CRED_INST_ID FROM TRANSACTION " +
                    "WHERE SESSION_ID = ? AND TX_CENTER_ID = ? AND TX_CURRENCY = ? AND CRED_INST_ID IS NOT NULL";

            for (String currency : distinctCurrencies) {
                List<String> institutions = jdbcTemplate.queryForList(institutionsSql, String.class,
                        sessionId, centerId, currency, sessionId, centerId, currency);
                institutions.stream()
                        .filter(instId -> instId != null && !instId.trim().isEmpty())
                        .forEach(instId -> pairs.add(new CurrencyInstitutionPair(currency, instId)));
            }

            log.info("Found {} currency-institution pairs to process.", pairs.size());


            contribution.getStepExecution().getJobExecution().getExecutionContext()
                    .put(CURRENCY_INSTITUTION_PAIRS_KEY, pairs);
            contribution.getStepExecution().getJobExecution().getExecutionContext()
                    .put(DISTINCT_CURRENCIES_KEY, new HashSet<>(distinctCurrencies));

            return RepeatStatus.FINISHED;
        }
    }
