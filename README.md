# Modulith Inventory Management System [WIP]

## Overview

This project serves as an **example of Domain-Driven Design (DDD)** and **Hexagonal Architecture** using **Spring Modulith**. The system is designed to demonstrate clean modular architecture with clear boundaries between different domains such as `product`, `inventory`, `stock`, `order`, and `search`, while promoting loose coupling through the use of Spring's event-driven architecture.

The project is implemented using **Spring Boot**, **PostgreSQL**, **Lombok**, **Spring Events**, and **ArchUnit** for architecture verification. By leveraging **Spring Modulith**, we ensure that each module operates independently, only communicating via domain events, thereby strictly following **Hexagonal Architecture** principles.

## Key Concepts

1. **Domain-Driven Design (DDD)**:
    - The core of the project revolves around key domain models such as `Product`, `Inventory`, and related business logic.
    - Each domain is encapsulated within its own module, with events ensuring the communication between modules without direct dependencies.

2. **Hexagonal Architecture (Ports & Adapters)**:
    - The project adopts a **Hexagonal Architecture** (also known as Ports and Adapters), ensuring a clear separation between the business logic (core domain) and external systems (e.g., databases, event publishers).
    - Interfaces (Ports) are defined for repositories and services to decouple the core business logic from their implementations.

3. **Event-Driven Architecture**:
    - Each domain module communicates with other modules through **externalized Spring Events**. This promotes loose coupling and allows for independent evolvability.
    - Event listeners react to domain events such as `ProductCreatedEvent`, `ProductDeletedEvent`, and `InventoryUpdatedEvent`.

4. **Modulith Approach**:
    - **Spring Modulith** is used to divide the application into distinct modules, each with its own responsibility, and enforce clear boundaries.
    - There are no direct dependencies between domain packages; instead, they communicate through events.

5. **ArchUnit for Architecture Verification**:
    - The architecture of the project is tested using **ArchUnit**, ensuring that no package violates the rules of modularity, such as unwanted dependencies between modules or cyclic references.
    - The `ArchitectureTest` class ensures that each module only accesses its own root package or explicitly allowed packages and verifies the absence of cyclic dependencies.

## Features

1. **Product Management**:
    - Full CRUD operations for managing products.
    - Handles product statuses such as `ACTIVE`, `DISCONTINUED`, etc.
    - Listens for product lifecycle events (e.g., `ProductCreatedEvent`, `ProductDeletedEvent`) and triggers corresponding actions.

2. **Inventory Management**:
    - Initializes and updates inventory based on product actions.
    - Inventory is automatically updated when product stock changes.
    - Event-driven synchronization with the `Product` domain.

3. **Spring Events**:
    - **Product and Inventory** modules communicate via Spring events such as `ProductCreatedEvent` and `InventoryUpdatedEvent`.
    - Events decouple modules and ensure that they are independent, adhering to the **Hexagonal Architecture** approach.

4. **PostgreSQL Database**:
    - Data persistence is handled via **PostgreSQL**.
    - Separate repository ports and adapters ensure the **Hexagonal Architecture** is maintained by keeping the database as an external detail.

5. **Actuator**:
    - **Spring Actuator** is included for monitoring and health check purposes, exposing detailed application metrics.

## Architecture Overview

The project follows **Domain-Driven Design (DDD)** with **Hexagonal Architecture** principles, ensuring a clean separation of concerns between the core domain logic and external systems like databases or messaging systems.

- **Domain Layer**: Represents the core business logic and domain models.
- **Application Layer**: Contains the business use cases and orchestrates domain activities.
- **Infrastructure Layer**: Contains the adapters for communication with external systems, such as databases (repository adapters).
- **Ports**: Define the interfaces for communicating with the domain layer (both inbound and outbound).
- **Adapters**: Implement the ports to integrate with external systems like the database.
