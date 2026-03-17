package com.harshit.vaultledger.service;

import com.harshit.vaultledger.dto.AccountRequest;
import com.harshit.vaultledger.dto.AccountResponse;
import com.harshit.vaultledger.exception.AccountNotFoundException;
import com.harshit.vaultledger.model.Account;
import com.harshit.vaultledger.model.User;
import com.harshit.vaultledger.repository.AccountRepository;
import com.harshit.vaultledger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        User owner = userRepository.findByUsername(request.ownerUsername())
                .orElseThrow(() -> new UsernameNotFoundException(request.ownerUsername()));

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .owner(owner)
                .balance(request.initialBalance())
                .currency(request.currency() != null ? request.currency() : "USD")
                .build();

        Account saved = accountRepository.save(account);
        return AccountResponse.from(saved);
    }

    public AccountResponse getAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        return AccountResponse.from(account);
    }

    public AccountResponse getAccountForUser(Long id, String username) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        if (!account.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Account does not belong to user");
        }
        return AccountResponse.from(account);
    }

    public List<AccountResponse> getAccountsForUser(String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return accountRepository.findByOwnerId(owner.getId()).stream()
                .map(AccountResponse::from)
                .toList();
    }

    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(AccountResponse::from)
                .toList();
    }

    private String generateAccountNumber() {
        String raw = UUID.randomUUID().toString().replace("-", "");
        String candidate = "VL-" + raw.substring(0, 8).toUpperCase();
        while (accountRepository.existsByAccountNumber(candidate)) {
            raw = UUID.randomUUID().toString().replace("-", "");
            candidate = "VL-" + raw.substring(0, 8).toUpperCase();
        }
        return candidate;
    }
}
