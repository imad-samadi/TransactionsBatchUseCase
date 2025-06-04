package com.S2M.TransactionsBatchUseCase.Repo;

import com.S2M.TransactionsBatchUseCase.Entity.Repport.WalletActivityReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletActivityReportRepository extends JpaRepository<WalletActivityReport,Long> {
}
