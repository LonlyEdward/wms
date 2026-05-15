
# WMS - Wholesale Management System

A full stack wholesale operations platform that replaces spreadsheets, WhatsApp orders, and manual invoicing with a single configurable internal web application. Built for wholesale and distribution businesses of any product type, the system manages the complete order lifecycle from placement through warehouse fulfilment, invoicing, payment collection and reporting. Built backend first with Spring Boot and React. No mock data layer, every page calls a real REST API backed by PostgreSQL from day one.

## Features

- Order management with a full 7-stage lifecycle, status machine, and immutable audit trail
- Real-time inventory tracking with stock reservation, movement ledger, and low-stock alerts
- Auto-generated tax invoices delivered as professionally formatted PDFs via iText 7
- Online payment processing via Flutterwave (East Africa) and Stripe (international) in sandbox mode
- Simulated Rafiki cross-border payment layer demonstrating the gateway abstraction pattern
- Warehouse pick, pack, and dispatch workflow with auto-generated dispatch note PDFs
- Async multi-channel notification engine via Spring Events — email, SMS, WhatsApp, and in-app
- B2B buyer portal with Google OAuth2 social login and self-service order placement
- Sales, inventory, financial, and fulfilment reports with live Recharts visualisations and CSV export
- Staff productivity monitoring and exception reporting derived from the audit log
- Returns and refunds module with RMA workflow, stock restock, and automatic credit note generation
- Tiered pricing engine with per-customer price lists, override prices, and minimum order quantities
- Delivery tracking module with driver assignment and proof-of-delivery photo capture
- Role-based access control across four roles — Admin, Warehouse, Accounts, and Buyer
- Flyway-managed database schema with 14 versioned migration files and rich seed data
- Webhook signature verification for both Flutterwave and Stripe payment callbacks
- Full integration test suite using Testcontainers running against a real PostgreSQL instance
- Dockerised full stack — entire system starts with a single docker compose up command
- GitHub Actions CI pipeline running on every push with a PostgreSQL service container


## Tech Stack

**Client:** React, TailwindCSS

**Server:** Spring Boot

