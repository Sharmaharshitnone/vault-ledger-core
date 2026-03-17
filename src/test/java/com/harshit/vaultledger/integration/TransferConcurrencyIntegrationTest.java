package com.harshit.vaultledger.integration;

import com.harshit.vaultledger.dto.TransferRequest;
import com.harshit.vaultledger.exception.InsufficientFundsException;
import com.harshit.vaultledger.model.*;
import com.harshit.vaultledger.repository.AccountRepository;
import com.harshit.vaultledger.repository.UserRepository;
import com.harshit.vaultledger.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TransferConcurrencyIntegrationTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private Account source;
    private Account destination;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .username("testuser_" + System.nanoTime())
                .email("test" + System.nanoTime() + "@test.com")
                .password("encoded")
                .role(Role.ROLE_USER)
                .build());

        source = accountRepository.save(Account.builder()
                .accountNumber("SRC-" + System.nanoTime())
                .owner(user)
                .balance(new BigDecimal("1000.00"))
                .build());

        destination = accountRepository.save(Account.builder()
                .accountNumber("DST-" + System.nanoTime())
                .owner(user)
                .balance(new BigDecimal("0.00"))
                .build());
    }

    @Test
    void concurrentTransfers_preventDoubleSpending() throws Exception {
        int threadCount = 10;
        BigDecimal transferAmount = new BigDecimal("200.00");
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    transferService.executeTransfer(new TransferRequest(
                            source.getId(), destination.getId(),
                            transferAmount, "concurrent test"));
                    successCount.incrementAndGet();
                } catch (InsufficientFundsException e) {
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            }));
        }

        startLatch.countDown();

        for (Future<?> f : futures) {
            f.get(30, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(5);
        assertThat(failCount.get()).isEqualTo(5);

        Account updatedSource = accountRepository.findById(source.getId()).orElseThrow();
        Account updatedDest = accountRepository.findById(destination.getId()).orElseThrow();

        assertThat(updatedSource.getBalance()).isEqualByComparingTo("0.00");
        assertThat(updatedDest.getBalance()).isEqualByComparingTo("1000.00");

        // Conservation of money
        assertThat(updatedSource.getBalance().add(updatedDest.getBalance()))
                .isEqualByComparingTo("1000.00");
    }

    @Test
    void bidirectionalTransfers_noDeadlock() throws Exception {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger completedCount = new AtomicInteger(0);

        source.setBalance(new BigDecimal("10000.00"));
        destination.setBalance(new BigDecimal("10000.00"));
        accountRepository.save(source);
        accountRepository.save(destination);

        BigDecimal smallAmount = new BigDecimal("1.00");
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final boolean forward = (i % 2 == 0);
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    Long src = forward ? source.getId() : destination.getId();
                    Long dst = forward ? destination.getId() : source.getId();
                    transferService.executeTransfer(
                            new TransferRequest(src, dst, smallAmount, "deadlock test"));
                    completedCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        startLatch.countDown();

        for (Future<?> f : futures) {
            f.get(30, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertThat(completedCount.get()).isEqualTo(threadCount);

        Account updatedSource = accountRepository.findById(source.getId()).orElseThrow();
        Account updatedDest = accountRepository.findById(destination.getId()).orElseThrow();
        assertThat(updatedSource.getBalance().add(updatedDest.getBalance()))
                .isEqualByComparingTo("20000.00");
    }
}
