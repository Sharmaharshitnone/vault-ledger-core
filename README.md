# Vault-Ledger Core

Vault-Ledger is a distributed banking ledger API designed for high-integrity financial transactions. The system ensures ACID compliance to maintain data consistency in high-concurrency environments.

## System Overview

- **Objective**: Development of a secure ledger capable of managing concurrent financial transactions without data corruption.
- **Impact**: Optimized database interaction through Hibernate connection pooling, achieving sub-50ms latency for ledger updates.
- **Compliance**: Strict adherence to ACID principles to prevent race conditions and double-spending.

## Technical Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3
- **Security**: Spring Security (JWT, RBAC)
- **Data Layer**: PostgreSQL, Hibernate
- **Deployment**: Docker

## Key Functionalities

- **Stateless Authentication**: Secure Role-Based Access Control (RBAC) implemented via JWT.
- **Concurrency Management**: Transaction isolation and locking strategies to prevent race conditions.
- **Transactional Integrity**: Reliable rollback mechanisms to ensure ledger consistency.
- **High Throughput**: Connection pool optimization for rapid state transitions.

## Installation and Execution

1. **Database Environment**:
   ```bash
   docker-compose up -d
   ```
2. **Build and Run**:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

## Engineering Challenges

- Designing stateless security architectures for sensitive financial data.
- Implementing reliable transaction recovery and rollback protocols.
