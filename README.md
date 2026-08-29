# Enterprise Order Platform

A production-oriented, event-driven order and inventory platform built as a senior software engineering portfolio project.

> Status: foundation established. Features are intentionally delivered through focused pull requests so architecture, testing, reliability, security, infrastructure, and operational decisions remain reviewable.

## Why this project exists

This repository demonstrates how an enterprise platform can evolve from a small service foundation into a resilient distributed system. The emphasis is on engineering decisions that matter in production: service boundaries, data ownership, asynchronous workflows, idempotency, observability, security, testing, deployment, and failure recovery.

## Technology direction

- Java 25 LTS
- Spring Boot 4.1
- Maven multi-module build
- PostgreSQL
- Apache Kafka
- Redis
- Testcontainers
- Docker
- Kubernetes + Helm
- GitHub Actions
- React + TypeScript operations console (planned)
- AWS-oriented deployment patterns (planned)

## Services

| Service | Responsibility | Local port |
|---|---|---:|
| Order Service | Order lifecycle and customer-facing order APIs | 8081 |
| Inventory Service | Stock and reservation ownership | 8082 |
| Notification Service | Asynchronous customer notifications | 8083 |

## Architecture

```text
                         +----------------------+
                         | React Operations UI  |
                         +----------+-----------+
                                    |
                                    v
+-------------+             +-------+--------+
| PostgreSQL  | <---------- | Order Service  |
+-------------+             +-------+--------+
                                    |
                              domain events
                                    |
                                    v
                              +-----+-----+
                              |   Kafka   |
                              +--+-----+--+
                                 |     |
                   +-------------+     +----------------+
                   v                                    v
          +--------+---------+                  +--------+---------+
          | Inventory Service|                  |Notification Svc. |
          +--------+---------+                  +------------------+
                   |
                   v
             +-----+-----+
             | PostgreSQL|
             +-----------+
```

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the evolving design.

## Local infrastructure

```bash
docker compose up -d
```

The initial compose stack provides PostgreSQL, Redis, and Kafka. Service persistence and messaging integrations are added through focused feature PRs.

## Build

Prerequisites:

- JDK 25
- Maven 3.9+ (or Maven Wrapper once added)
- Docker for integration infrastructure

```bash
mvn clean verify
```

## Engineering workflow

All substantial changes should be issue-driven and merged through pull requests. See:

- [`docs/PR_ROADMAP.md`](docs/PR_ROADMAP.md)
- [`docs/GITHUB_SETUP.md`](docs/GITHUB_SETUP.md)
- [Pull request template](.github/PULL_REQUEST_TEMPLATE.md)

## What this repository will demonstrate

- REST API design and validation
- Domain-driven modeling
- PostgreSQL persistence and schema migrations
- Kafka producers/consumers
- transactional outbox
- saga-style workflows
- idempotency and retry handling
- Redis caching
- JWT and RBAC
- structured logging, metrics, and tracing
- Testcontainers and end-to-end tests
- Docker/Kubernetes/Helm
- CI/CD and security scanning
- React/TypeScript operations UI
- architecture decision records and operational runbooks

## License

MIT
