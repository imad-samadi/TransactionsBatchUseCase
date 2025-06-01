package com.S2M.TransactionsBatchUseCase.Config.Batch;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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


    @NotNull
    private String sessionId;

    @NotBlank
    private String centerId;

    private String TransactionInputFile ="C:/Users/msi/Downloads/TransactionsBatchUseCase (1)/TransactionsBatchUseCase/src/main/resources/transactions_part1.csv" ;

    private String feeInfoInputFile ="C:/Users/msi/Downloads/TransactionsBatchUseCase (1)/TransactionsBatchUseCase/src/main/resources/fees_generated.csv";


    private int corePoolSize = 10 ;

    private int maxPoolSize = 30 ;


}
