package com.harshit.vaultledger.dto;

import com.harshit.vaultledger.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(
    String transactionReference,
    Long sourceAccountId,
    Long destinationAccountId,
    BigDecimal amount,
    String status,
    Instant timestamp
) {
    public static TransferResponse from(Transaction txn) {
        return new TransferResponse(
            txn.getTransactionReference(),
            txn.getSourceAccount().getId(),
            txn.getDestinationAccount().getId(),
            txn.getAmount(),
            txn.getStatus().name(),
            txn.getCompletedAt()
        );
    }
}
