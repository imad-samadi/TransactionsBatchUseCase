package com.S2M.TransactionsBatchUseCase;

import com.S2M.TransactionsBatchUseCase.Config.Batch.BatchProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Component
@Slf4j

public class ReportJobRunner implements CommandLineRunner {

    private final JobLauncher jobLauncher;
    private final Job walletActivityReportingJob;
    private final BatchProperties batchProperties; // Inject your properties bean

    @Autowired
    public ReportJobRunner(JobLauncher jobLauncher,
                           @Qualifier("walletActivityReportingJob") Job walletActivityReportingJob,
                           BatchProperties batchProperties) { // Autowire BatchProperties
        this.jobLauncher = jobLauncher;
        this.walletActivityReportingJob = walletActivityReportingJob;
        this.batchProperties = batchProperties; // Store it
    }

    @Override
    public void run(String... args) {
        log.info("Attempting to start Wallet Activity Reporting Job via CommandLineRunner...");

        // Use properties from BatchProperties if available, otherwise provide fallbacks or make them mandatory
        String sessionId = batchProperties.getSessionId();
        String centerId = batchProperties.getCenterId();

        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("sessionId not configured in batch.job.defaults, using a hardcoded fallback for runner.");
            sessionId = "FALLBACK_SESS_001";
        }
        if (centerId == null || centerId.trim().isEmpty()) {
            log.warn("centerId not configured in batch.job.defaults, using a hardcoded fallback for runner.");
            centerId = "FALLBACK_CENTER_A";
        }

        SimpleDateFormat jobParamDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
        String sessionDateStrForParam = "2023-11-15 00:00:00.000"; // Example fixed date for testing
        Date sessionDateForJob;
        try {
            sessionDateForJob = jobParamDateFormat.parse(sessionDateStrForParam);
        } catch (Exception e) {
            log.warn("Could not parse fixed date string, using current date for job parameter.", e);
            sessionDateForJob = new Date();
        }

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("sessionId", sessionId) // Use value from properties
                .addString("centerId", centerId)   // Use value from properties
                .addDate("sessionDate", sessionDateForJob, true)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        try {

            org.springframework.batch.core.JobExecution jobExecution = jobLauncher.run(walletActivityReportingJob, jobParameters);

        } catch (JobExecutionAlreadyRunningException e) {
            log.error("JobExecutionAlreadyRunningException: Job is already running.", e);
        } catch (JobRestartException e) {
            log.error("JobRestartException: Job cannot be restarted.", e);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.info("JobInstanceAlreadyCompleteException: Job already completed successfully with these identifying parameters.");
        } catch (JobParametersInvalidException e) {
            log.error("JobParametersInvalidException: Invalid job parameters.", e);
        } catch (Exception e) {
            log.error("Generic Exception during job launch or execution.", e);
        }
    }
}
