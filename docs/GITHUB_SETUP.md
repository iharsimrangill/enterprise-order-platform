# GitHub Setup

## Repository

Suggested repository name: `enterprise-order-platform`

Suggested description:

> Event-driven enterprise order and inventory platform built with Java, Spring Boot, Kafka, PostgreSQL, Redis, Docker, Kubernetes and AWS-oriented deployment patterns.

Suggested topics:

`java` `spring-boot` `microservices` `kafka` `postgresql` `redis` `docker` `kubernetes` `aws` `event-driven-architecture` `system-design`

## Branch protection

For `main`, enable:

- require a pull request before merging
- require at least one approving review when collaborators are available
- require status checks to pass
- require conversation resolution before merge
- block force pushes
- block deletion

For a solo portfolio repository, do not manufacture fake reviewers. A clean PR description plus passing CI is preferable.

## Branch naming

- `feat/...`
- `fix/...`
- `refactor/...`
- `test/...`
- `perf/...`
- `sec/...`
- `infra/...`
- `docs/...`

## Commit convention

Use conventional, specific commits:

- `feat(order): add order creation endpoint`
- `test(order): cover invalid order quantities`
- `fix(inventory): prevent duplicate reservation`
- `infra(ci): cache Maven dependencies`
- `docs(adr): document transactional outbox decision`
