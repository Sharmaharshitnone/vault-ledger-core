package com.harshit.vaultledger.service;

import com.harshit.vaultledger.dto.TransferRequest;
import com.harshit.vaultledger.exception.AccountNotFoundException;
import com.harshit.vaultledger.exception.InsufficientFundsException;
import com.harshit.vaultledger.model.*;
import com.harshit.vaultledger.repository.AccountRepository;
import com.harshit.vaultledger.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferService transferService;

    @Test
    void executeTransfer_success() {
        Account source = buildAccount(1L, "1000.00");
        Account dest = buildAccount(2L, "500.00");

        when(accountRepository.findAllByIdForUpdateOrdered(List.of(1L, 2L)))
                .thenReturn(List.of(source, dest));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TransferRequest request = new TransferRequest(1L, 2L,
                new BigDecimal("200.00"), "test transfer");

        transferService.executeTransfer(request);

        assertThat(source.getBalance()).isEqualByComparingTo("800.00");
        assertThat(dest.getBalance()).isEqualByComparingTo("700.00");
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void executeTransfer_insufficientFunds_throws() {
        Account source = buildAccount(1L, "50.00");
        Account dest = buildAccount(2L, "500.00");

        when(accountRepository.findAllByIdForUpdateOrdered(List.of(1L, 2L)))
                .thenReturn(List.of(source, dest));

        TransferRequest request = new TransferRequest(1L, 2L,
                new BigDecimal("200.00"), null);

        assertThatThrownBy(() -> transferService.executeTransfer(request))
                .isInstanceOf(InsufficientFundsException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void executeTransfer_sameAccount_throws() {
        TransferRequest request = new TransferRequest(1L, 1L,
                new BigDecimal("100.00"), null);

        assertThatThrownBy(() -> transferService.executeTransfer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different");
    }

    @Test
    void executeTransfer_sourceNotFound_throws() {
        when(accountRepository.findAllByIdForUpdateOrdered(List.of(1L, 2L)))
                .thenReturn(Collections.singletonList(buildAccount(2L, "1000.00")));

        TransferRequest request = new TransferRequest(1L, 2L,
                new BigDecimal("100.00"), null);

        assertThatThrownBy(() -> transferService.executeTransfer(request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void executeTransfer_negativeAmount_throws() {
        TransferRequest request = new TransferRequest(1L, 2L,
                new BigDecimal("-50.00"), null);

        assertThatThrownBy(() -> transferService.executeTransfer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void executeTransfer_frozenSourceAccount_throws() {
        Account source = buildAccount(1L, "1000.00");
        source.setStatus(AccountStatus.FROZEN);
        Account dest = buildAccount(2L, "500.00");

        when(accountRepository.findAllByIdForUpdateOrdered(List.of(1L, 2L)))
                .thenReturn(List.of(source, dest));

        TransferRequest request = new TransferRequest(1L, 2L,
                new BigDecimal("100.00"), null);

        assertThatThrownBy(() -> transferService.executeTransfer(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not active");
    }

    private Account buildAccount(Long id, String balance) {
        Account a = new Account();
        a.setId(id);
        a.setBalance(new BigDecimal(balance));
        a.setStatus(AccountStatus.ACTIVE);
        a.setVersion(0L);
        return a;
    }
}
