package com.harshit.vaultledger.service;

import com.harshit.vaultledger.dto.TransferRequest;
import com.harshit.vaultledger.dto.TransferResponse;
import com.harshit.vaultledger.exception.AccountNotFoundException;
import com.harshit.vaultledger.exception.InsufficientFundsException;
import com.harshit.vaultledger.model.*;
import com.harshit.vaultledger.repository.AccountRepository;
import com.harshit.vaultledger.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Execute an atomic fund transfer between two accounts.
     *
     * Concurrency strategy (defense in depth):
     * 1. REPEATABLE_READ isolation prevents non-repeatable reads.
     * 2. Pessimistic locking (SELECT FOR UPDATE) acquires exclusive row locks.
     * 3. Ordered lock acquisition (lower ID first) prevents deadlocks.
     * 4. Optimistic locking (@Version) acts as a secondary safety net.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TransferResponse executeTransfer(TransferRequest request) {
        Long sourceId = request.sourceAccountId();
        Long destinationId = request.destinationAccountId();
        BigDecimal amount = request.amount();

        if (sourceId.equals(destinationId)) {
            throw new IllegalArgumentException(
                    "Source and destination accounts must be different");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transfer amount must be positive");
        }

        // Acquire pessimistic locks in consistent order to prevent deadlocks
        Long firstId = Math.min(sourceId, destinationId);
        Long secondId = Math.max(sourceId, destinationId);

        List<Account> lockedAccounts = accountRepository
                .findAllByIdForUpdateOrdered(List.of(firstId, secondId));

        if (lockedAccounts.size() != 2) {
            boolean sourceExists = lockedAccounts.stream()
                    .anyMatch(a -> a.getId().equals(sourceId));
            boolean destExists = lockedAccounts.stream()
                    .anyMatch(a -> a.getId().equals(destinationId));
            if (!sourceExists) throw new AccountNotFoundException(sourceId);
            if (!destExists) throw new AccountNotFoundException(destinationId);
        }

        Account source = lockedAccounts.stream()
                .filter(a -> a.getId().equals(sourceId))
                .findFirst().orElseThrow();
        Account destination = lockedAccounts.stream()
                .filter(a -> a.getId().equals(destinationId))
                .findFirst().orElseThrow();

        if (source.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Source account " + sourceId + " is not active");
        }
        if (destination.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Destination account " + destinationId + " is not active");
        }

        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    sourceId, amount, source.getBalance());
        }

        // Atomic balance mutation
        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));

        accountRepository.save(source);
        accountRepository.save(destination);

        // Immutable audit trail
        String reference = "TXN-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();

        Transaction transaction = Transaction.builder()
                .transactionReference(reference)
                .sourceAccount(source)
                .destinationAccount(destination)
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .description(request.description())
                .balanceAfterSource(source.getBalance())
                .balanceAfterDestination(destination.getBalance())
                .completedAt(Instant.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        log.info("Transfer completed: {} -> {}, amount={}, ref={}",
                sourceId, destinationId, amount, reference);

        return TransferResponse.from(saved);
    }
}
