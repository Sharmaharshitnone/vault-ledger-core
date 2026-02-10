# Vault-Ledger Core 🏦

A secure, distributed banking ledger API built with ACID compliance and high concurrency in mind.

## 🚀 Overview

- **Problem Solved**: Addresses the challenge of data consistency in concurrent financial transactions, effectively preventing double-spending and race conditions.
- **Impact**: Achieved sub-50ms latency for ledger updates through optimized database queries and Hibernate connection pooling.
- **Compliance**: Strict adherence to ACID properties for all financial operations.

## 🛠 Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3
- **Security**: Spring Security (JWT, RBAC)
- **Persistence**: PostgreSQL, Hibernate
- **Containerization**: Docker

## 🔑 Key Features

- **Stateless Authentication**: Robust JWT-based RBAC (Role-Based Access Control).
- **Concurrency Control**: Robust handling of race conditions in financial transactions.
- **Reliable Rollbacks**: Implementation of transactional integrity and rollback mechanisms.
- **Optimized Performance**: Fine-tuned Hibernate configuration for high-throughput ledger operations.

## 🏗 Setup

1. **Database**:
   ```bash
   docker-compose up -d
   ```
2. **Build & Run**:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

## 🧠 Challenges Overcome

- Implementing stateless authentication with Spring Security.
- Designing reliable transaction rollback mechanisms for complex financial flows.
