package com.harshit.vaultledger.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
    @NotNull(message = "Source account ID is required")
    Long sourceAccountId,

    @NotNull(message = "Destination account ID is required")
    Long destinationAccountId,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Minimum transfer amount is 0.01")
    BigDecimal amount,

    String description
) {}
