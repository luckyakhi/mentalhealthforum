# ADR-004: Event-Driven Inter-Service Communication

**Status**: Accepted
**Date**: 2026-04-05
**Decision Makers**: Core Team

## Context
Microservices need to communicate. We need to choose between synchronous (HTTP/gRPC) and asynchronous (event-driven) patterns.

## Decision
All inter-service communication uses **asynchronous events via Apache Kafka**. No direct HTTP calls between services (only through the API Gateway for client-facing requests).

## Rationale
- **Decoupling**: Services don't need to know about each other's existence
- **Resilience**: If Moderation Service is down, Forum Service still works (events queue)
- **Scalability**: Consumers process at their own pace
- **Audit trail**: Kafka retains events for replay and debugging
- **Mental health context**: Content safety events (crisis detection) need reliable delivery

### Why Kafka
- Industry standard for event streaming
- Durable message storage with configurable retention
- Consumer groups for parallel processing
- Dead letter queues for failed messages
- Amazon MSK (managed) for production

### Alternatives Considered
- **RabbitMQ**: Simpler but weaker durability and replay capabilities
- **AWS SQS/SNS**: Vendor lock-in, less flexible topic model
- **gRPC**: Fast but creates tight coupling between services

## Consequences
- **Positive**: Services are independently deployable and resilient
- **Positive**: Event log provides audit trail and replay capability
- **Negative**: Eventual consistency — data is not immediately consistent across services
- **Negative**: Kafka operational complexity (mitigated by MSK in prod, Redpanda locally)
- **Mitigation**: CloudEvents envelope for schema standardization
