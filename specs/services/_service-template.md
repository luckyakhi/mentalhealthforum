# Service Spec: {Service Name}

> **Status**: Draft | In Review | Approved | Implemented
> **Owner**: {team/person}
> **Phase**: {1-5}
> **Last Updated**: YYYY-MM-DD

## Purpose
{One paragraph describing what this service does and why it exists.}

## Domain Model

### Aggregate Roots
{List aggregate roots with their key fields and invariants}

### Value Objects
{List value objects}

### Domain Events (Published)
{Events this service publishes}

### Domain Events (Consumed)
{Events this service listens to}

## API Endpoints

| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| | | | |

## Database Tables
{Key tables with columns, or reference to data-architecture.md}

## Dependencies
- **Inbound**: {Who calls this service}
- **Outbound**: {What this service calls/publishes to}

## Non-Functional Requirements
- **Latency**: p99 < {X}ms
- **Throughput**: {X} requests/second
- **Availability**: {X}%
- **Data Retention**: {X} days/years

## Configuration
{Environment variables, feature flags}

## Acceptance Criteria
{Link to user stories or inline criteria}

## Open Questions
{Unresolved decisions}
