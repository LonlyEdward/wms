# WMS - Wholesale Management System

> A REST API for wholesale distribution businesses. Built with Spring Boot 3.4.5, Java 21 and PostgreSQL 16.
 
---

## Overview

Wholesale businesses in Tanzania and across East Africa manage their daily operations manually, orders written in notebooks, stock counted by hand, invoices generated in Excel and payment tracking done through WhatsApp. This creates real operational problems: stock gets oversold, invoices get lost, credit is extended beyond safe limits, and business owners have no visibility into what is happening until something goes wrong.

WMS is a backend API that digitizes and automates the complete operation of a wholesale distribution business. It handles everything from the moment a customer places an order through stock management, invoicing, payment collection, and financial reporting all through a single, consistent and secure REST API.

The system is architected as a multi-tenant SaaS platform, meaning a single deployment can serve multiple businesses simultaneously with complete data isolation between them.
 
---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.5 |
| Database | PostgreSQL 16 |
| Schema Management | Flyway |
| ORM | Hibernate / Spring Data JPA |
| Security | Spring Security + JWT |
| PDF Generation | iText 7 |
| Email | Spring Mail (Mailhog in development) |
| Containerization | Docker Compose |
| API Documentation | Springdoc OpenAPI / Swagger UI |
| Build Tool | Maven |
 
---

## Architecture

The system is organized into four distinct layers. Every HTTP request travels through all four in sequence.

```
┌─────────────────────────────────────────────┐
│              Security Layer                 │
│   JWT Auth Filter · Role Enforcement · CORS │
├─────────────────────────────────────────────┤
│              API Layer (Controllers)        │
│   HTTP Routing · Validation · Response      │
├─────────────────────────────────────────────┤
│         Business Logic Layer (Services)     │
│   Business Rules · Transactions · Events    │
├─────────────────────────────────────────────┤
│           Data Layer (Repositories)         │
│   JPA Entities · Spring Data · PostgreSQL   │
└─────────────────────────────────────────────┘
```

### Security Layer
Every request is authenticated via JWT before reaching any controller. `JwtAuthFilter` validates the token signature, checks expiry, and loads the live user from the database on every request. Role-based access control is enforced at the service layer through Spring AOP  `@PreAuthorize` annotations ensure methods can only be called by users with the required role, regardless of how the method is invoked.

### API Layer
Controllers are deliberately thin. Each method validates the incoming request with `@Valid`, delegates to exactly one service method and wraps the result in a consistent `ApiResponse<T>` envelope. All responses success and error share the same JSON shape.

### Business Logic Layer
All business rules live in services. `@Transactional` boundaries ensure every operation is atomic either everything succeeds or everything rolls back. No partial state ever reaches the database.

### Data Layer
Every table has a corresponding `@Entity` class. All entities except `Business` extend `BaseEntity` which provides `id`, `businessId`, `createdAt`, and `updatedAt` automatically. Every query is scoped by `businessId`, tenant isolation is enforced at the data layer, not just in application code.
 
---

## Features

### Multi-Tenant SaaS Architecture
Every record is owned by a `Business`. Every query is automatically scoped by `businessId` extracted from the current user's JWT. A user from Business A cannot access Business B's data regardless of what ID they provide in the request. One deployment serves multiple businesses with complete data isolation.

### Authentication & Security
- Stateless JWT authentication, access tokens expire in 15 minutes, refresh tokens are stored as SHA-256 hashes and are explicitly revocable on logout
- BCrypt password hashing with work factor 12
- Role-based access control enforced at the service layer via Spring AOP with five roles: `ADMIN`, `WAREHOUSE`, `ACCOUNTS`, `DRIVER`, `BUYER`
- All secrets loaded from environment variables via dotenv, never hardcoded

### Product & Inventory Management
- Hierarchical product categories with parent-child tree structure
- Products carry SKU, cost price, sale price, reorder point, and inventory tracking flag
- Stock ledger pattern, stock is never a stored column. Every change (receipt, reservation, dispatch, return) is an immutable row in `stock_movements`. Current stock is computed as the sum of all movements, giving a permanent audit trail and eliminating race conditions from concurrent orders
- Available stock = current stock minus reserved stock customers can never be oversold

### Customer Management
- Credit limits enforced at order creation, outstanding balance across all unpaid invoices plus the new order total must not exceed the limit
- Account hold/release, suspended accounts are blocked from placing new orders
- Payment terms (NET_14, NET_30, NET_60) drive invoice due date calculation automatically

### Order Management
- Full order lifecycle enforced by `OrderStatusMachine` a pure, dependency-free class that validates every status transition before any side effects run
- Confirming an order reserves stock. Dispatching deducts it. Cancelling releases reservations
- Complete status history on every order with actor, timestamp, and reason
- Product and address snapshots captured at order time, historical records are immutable

### Invoicing & Payments
- Invoices auto-generated on order confirmation with PDF output via iText 7
- Invoice numbers from PostgreSQL sequences, atomic and concurrent-safe
- Daily scheduled job marks overdue invoices automatically every morning
- Partial payment support, invoice status progresses from `UNPAID -> PARTIAL -> PAID`
- Online payment integration with Flutterwave and Stripe via the Strategy pattern, adding a new gateway requires one new class and one line in a factory
- Webhook handlers verify signatures and are idempotent, duplicate webhooks are detected and ignored

### Immutable Audit Logging
Every data mutation is permanently recorded in `audit_log` with who, when, and the before/after state. `Propagation.REQUIRES_NEW` ensures audit entries commit in their own independent transaction, even if the outer operation fails and rolls back, the record of the attempt survives. Audit rows are never updated or deleted.

### Exception Handling
Centralized via `GlobalExceptionHandler` with a custom exception hierarchy, `EntityNotFoundException` (404), `BusinessRuleException` (422), `CreditLimitExceededException` (422), `AccountOnHoldException` (422), `ModuleNotEnabledException` (409). Every error carries a machine-readable `errorCode`. The catch-all handler logs full stack traces privately but returns only a generic message publicly, internal details are never leaked.
