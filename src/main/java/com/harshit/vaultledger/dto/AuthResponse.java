package com.harshit.vaultledger.dto;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {}
