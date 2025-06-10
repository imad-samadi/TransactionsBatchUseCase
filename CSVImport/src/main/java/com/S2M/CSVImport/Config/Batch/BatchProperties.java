package com.S2M.CSVImport.Config.Batch;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Validated
@ConfigurationProperties(prefix = "batch")
public class BatchProperties {


    private String TransactionInputFile ="C:/Users/msi/Desktop/New folder (3) - Copy/CSVImport/src/main/resources/transactions_part1.csv" ;

    private String feeInfoInputFile ="C:/Users/msi/Desktop/New folder (3) - Copy/CSVImport/src/main/resources/fees_generated.csv";


    private int CSVChunkSize = 5 ;


}
