package com.harshit.vaultledger.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(Long accountId, BigDecimal requested, BigDecimal available) {
        super(String.format(
            "Insufficient funds in account %d: requested=%s, available=%s",
            accountId, requested.toPlainString(), available.toPlainString()));
    }
}
