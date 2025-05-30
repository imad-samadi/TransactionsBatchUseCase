package com.S2M.TransactionsBatchUseCase;

import com.S2M.TransactionsBatchUseCase.Config.Batch.BatchProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BatchProperties.class)

public class TransactionsBatchUseCaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionsBatchUseCaseApplication.class, args);
	}

}
