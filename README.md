# Wholesale Management System

## Overview

The **Wholesale Management System** is a production-ready backend API designed to digitize and automate wholesale distribution operations.

Many small and medium-sized wholesalers still rely on manual processes such as handwritten order books, spreadsheet-based invoicing, manual stock counting, and payment tracking through messaging platforms. These approaches often lead to stock discrepancies, lost invoices, delayed deliveries, inaccurate financial records, and poor visibility into customer balances and inventory levels.

This project was built to solve those challenges by providing a centralized platform that manages the entire wholesale workflow—from order creation and warehouse operations to invoicing, payment collection, delivery tracking, and business reporting.

The goal of this project is not only to provide functional business automation but also to demonstrate clean architecture, enterprise-grade backend development practices, scalability, maintainability, and real-world software engineering principles.

---

# Business Features

## Order Management

The system manages the complete order lifecycle:

* Order creation
* Order confirmation
* Warehouse processing
* Picking and packing
* Dispatch
* Delivery
* Order closure

A business-rule-driven state machine validates every status transition.

Examples:

* Orders cannot be dispatched before they are packed.
* Orders cannot be confirmed if a customer's account is on hold.
* Invalid workflow transitions are automatically rejected.

---

## Inventory Management

Inventory is managed using an immutable stock movement ledger.

Instead of storing stock as a single editable quantity, every stock operation is recorded as a separate movement entry:

* Stock receiving
* Stock reservations
* Order dispatches
* Returns
* Adjustments

Each movement contains:

* Quantity
* Reason
* Timestamp
* User responsible

Benefits:

* Complete audit trail
* Historical inventory visibility
* Improved accountability
* Accurate stock tracking

Available inventory is calculated dynamically from the movement ledger.

---

## Invoicing & Payment Processing

Invoices are automatically generated when orders are confirmed.

Features include:

* Invoice generation
* PDF invoice export
* Outstanding balance tracking
* Partial payment support
* Overdue invoice monitoring

### Payment Gateway Integrations

The system integrates with:

* Flutterwave (Sandbox)
* Stripe (Sandbox)

Supported payment capabilities:

* Invoice payments
* Payment reconciliation
* Transaction tracking

---

## Warehouse Operations

The warehouse workflow mirrors real-world fulfilment processes.

Features include:

* Pick-list generation
* Picking status tracking
* Packing status tracking
* Dispatch tracking
* Delivery confirmation
* Proof-of-delivery photo capture

This ensures operational visibility throughout the fulfilment lifecycle.

---

## Reporting & Analytics

The platform provides business intelligence and operational reporting.

### Dashboard Summary

A single endpoint returns key business KPIs such as:

* Total sales
* Inventory metrics
* Outstanding balances
* Order fulfilment statistics

### Detailed Reports

The system supports:

1. Sales Reports
2. Inventory Valuation Reports
3. Financial Position Reports
4. Fulfilment Efficiency Reports

All reports can be exported as CSV files.

---

## Buyer Portal

Customers can access a dedicated self-service portal.

Capabilities include:

* Placing orders
* Tracking deliveries
* Viewing invoices
* Paying invoices online
* Managing notification preferences

### Authentication

Buyers authenticate using:

* Google OAuth2

### Data Security

Strict ownership rules are enforced.

A buyer can only access their own resources.

Unauthorized resource requests return a `404 Not Found` response to prevent information disclosure.

---

# Technology Stack

| Component            | Technology          |
| -------------------- | ------------------- |
| Language             | Java 21             |
| Framework            | Spring Boot 3.4     |
| Database             | PostgreSQL 16       |
| ORM                  | Spring Data JPA     |
| Security             | Spring Security     |
| Authentication       | JWT                 |
| Database Migrations  | Flyway              |
| Build Tool           | Maven               |
| Payment Integrations | Flutterwave, Stripe |
| API Style            | REST                |

---

# System Architecture

The application follows a strict three-layer architecture.

```text
Controller Layer
        ↓
Service Layer
        ↓
Repository Layer
```

## Controller Layer

Controllers are intentionally thin.

Responsibilities:

* Receive HTTP requests
* Validate request structure
* Delegate work to services
* Return API responses

Controllers contain no business logic.

---

## Service Layer

The service layer contains all business rules and application logic.

Examples include:

* Credit limit validation
* Stock availability checks
* Order workflow validation
* Invoice calculations

### Transaction Management

Business operations execute within `@Transactional` boundaries.

Benefits:

* Atomic operations
* Automatic rollback on failure
* Data consistency

---

## Repository Layer

Repositories manage all database interactions using Spring Data JPA.

Features:

* Derived query methods
* JPQL for advanced queries
* Clean data access abstraction

---

# Security

## JWT Authentication

The API uses stateless JWT authentication.

Each request includes a Bearer Token:

```http
Authorization: Bearer <jwt-token>
```

Authentication workflow:

1. Validate token signature
2. Verify expiration
3. Load current user from database
4. Check account status
5. Populate Spring Security Context

### Refresh Tokens

Refresh tokens are:

* Stored as SHA-256 hashes
* Revocable on logout
* Separated from access tokens

---

## Role-Based Access Control

Authorization is enforced using Spring AOP.

Protected methods are intercepted before execution.

The authorization aspect:

1. Reads the authenticated user's role
2. Verifies required permissions
3. Blocks unauthorized access
4. Prevents execution of protected business logic

This keeps authorization concerns separate from business code.

---

# Event-Driven Notifications

The system uses asynchronous domain events.

Example workflow:

```text
Order Confirmed
       ↓
Publish Domain Event
       ↓
Return HTTP Response
       ↓
Background Listener
       ↓
Send Notifications
```

Supported notification channels:

* Email
* SMS
* In-App Notifications

Benefits:

* Faster API responses
* Improved scalability
* Reduced request latency

---

# Modular Business Features

The platform supports optional business modules.

Available modules:

* Returns Module
* Pricing Module
* Delivery Module

Modules can be enabled or disabled per business.

### Module Enforcement

A custom AOP aspect intercepts methods annotated with:

```java
@ModuleRequired
```

The aspect:

1. Reads the module configuration
2. Verifies the module is enabled
3. Allows or blocks execution

Service methods remain unaware of module restrictions, keeping business logic clean and maintainable.

---

# Key Engineering Principles

* Clean Architecture
* Separation of Concerns
* Domain-Driven Business Logic
* Event-Driven Design
* Stateless Authentication
* Auditability
* Transactional Consistency
* Extensibility
* Scalability
* Security First
