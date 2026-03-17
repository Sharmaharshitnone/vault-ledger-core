package com.harshit.vaultledger.dto;

import com.harshit.vaultledger.model.Account;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
    Long id,
    String accountNumber,
    String ownerUsername,
    BigDecimal balance,
    String currency,
    String status,
    Instant createdAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getAccountNumber(),
            account.getOwner().getUsername(),
            account.getBalance(),
            account.getCurrency(),
            account.getStatus().name(),
            account.getCreatedAt()
        );
    }
}
