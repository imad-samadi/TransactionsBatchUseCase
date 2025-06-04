package com.S2M.CSVImport;

import com.S2M.CSVImport.Config.BatchProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.task.configuration.EnableTask;

@SpringBootApplication
@EnableConfigurationProperties(BatchProperties.class)
@EnableTask
public class CsvImportApplication {

	public static void main(String[] args) {
		SpringApplication.run(CsvImportApplication.class, args);
	}

}
