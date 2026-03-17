package com.harshit.vaultledger.repository;

import com.harshit.vaultledger.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

    /**
     * Acquire PESSIMISTIC_WRITE locks on multiple accounts in a single query.
     * ORDER BY a.id ASC ensures locks are always acquired in ascending ID order,
     * preventing deadlocks when concurrent transfers involve the same pair of
     * accounts in reverse order (A->B and B->A).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id IN :ids ORDER BY a.id ASC")
    List<Account> findAllByIdForUpdateOrdered(@Param("ids") List<Long> ids);

    List<Account> findByOwnerId(Long ownerId);

    boolean existsByAccountNumber(String accountNumber);
}
