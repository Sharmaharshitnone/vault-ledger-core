package com.harshit.vaultledger.dto;

import java.time.Instant;

public record ErrorResponse(
    int status,
    String error,
    String message,
    String path,
    Instant timestamp
) {}
