package com.S2M.TransactionsBatchUseCase.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CurrencyInstitutionPair implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String currency;
    private String institutionId;
}
