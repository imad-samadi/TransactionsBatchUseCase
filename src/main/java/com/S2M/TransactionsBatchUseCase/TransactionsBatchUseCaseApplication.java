package com.S2M.TransactionsBatchUseCase;

import com.S2M.TransactionsBatchUseCase.Config.Batch.BatchProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(BatchProperties.class)
@EnableTask
public class TransactionsBatchUseCaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionsBatchUseCaseApplication.class, args);
	}

}
