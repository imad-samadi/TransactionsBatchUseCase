package com.S2M.TransactionsBatchUseCase.Repo;

import com.S2M.TransactionsBatchUseCase.Entity.Repport.SettlementReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementReportRepository extends JpaRepository<SettlementReport, Long> {

}
