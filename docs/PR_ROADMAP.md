# Pull Request Roadmap — Repository 1

The objective is not to inflate activity. Each PR should represent a coherent engineering change that can be reviewed, tested, and discussed in an interview.

| # | Branch | PR title | Primary signal |
|---:|---|---|---|
| 1 | `chore/bootstrap-platform` | Bootstrap multi-module Spring Boot platform | Architecture / build |
| 2 | `feat/order-domain-model` | Model order aggregate and lifecycle states | DDD / Java |
| 3 | `feat/order-create-api` | Add validated order creation REST API | REST / validation |
| 4 | `test/order-api-integration` | Add order API integration test suite | Testing |
| 5 | `feat/order-persistence` | Persist orders with PostgreSQL and Flyway | JPA / SQL |
| 6 | `feat/inventory-domain` | Add inventory and reservation domain model | Domain modeling |
| 7 | `feat/inventory-reservation-api` | Expose inventory reservation API | API design |
| 8 | `feat/kafka-order-events` | Publish OrderCreated events to Kafka | Kafka |
| 9 | `feat/inventory-event-consumer` | Reserve inventory from order events | Event-driven design |
| 10 | `fix/inventory-idempotency` | Make reservation consumer idempotent | Reliability |
| 11 | `feat/outbox-pattern` | Add transactional outbox for order events | Distributed systems |
| 12 | `feat/order-saga` | Coordinate order confirmation and rejection | Saga pattern |
| 13 | `feat/redis-order-cache` | Cache hot order reads in Redis | Caching |
| 14 | `perf/order-query-indexes` | Tune order queries and database indexes | Performance |
| 15 | `feat/jwt-security` | Secure APIs with JWT authentication | Security |
| 16 | `feat/rbac` | Add admin and operations role authorization | Authorization |
| 17 | `feat/notification-consumer` | Send notifications from domain events | Async processing |
| 18 | `feat/observability` | Add metrics, tracing and structured logs | Observability |
| 19 | `test/testcontainers` | Add PostgreSQL and Kafka Testcontainers tests | Integration testing |
| 20 | `infra/docker-images` | Add production Dockerfiles for services | Containers |
| 21 | `infra/kubernetes-manifests` | Deploy services to Kubernetes | Kubernetes |
| 22 | `infra/helm-chart` | Package platform with Helm | Platform engineering |
| 23 | `infra/github-actions` | Add CI quality gates and image builds | CI/CD |
| 24 | `sec/dependency-scanning` | Add dependency and container security checks | DevSecOps |
| 25 | `docs/system-design` | Document scaling, failure modes and ADRs | Senior-level design |
| 26 | `feat/react-admin-shell` | Add React/TypeScript operations console | React |
| 27 | `feat/react-order-search` | Add paginated order search and filters | Front end / API |
| 28 | `feat/react-live-status` | Stream order status updates to the UI | Real time |
| 29 | `test/e2e-critical-flow` | Add end-to-end order fulfillment test | E2E testing |
| 30 | `release/v1-readiness` | Add release checklist, runbook and v1 docs | Ownership / operations |

## Portfolio contribution target

This repository: **30 meaningful PRs**.

Across four substantial repositories at a similar depth, the GitHub profile can exceed 100 PRs without looking artificial.
