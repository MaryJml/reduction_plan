# Reduction Plan Service
Build a RESTful service that manages customer limit reduction plans using event-driven architecture.

## Requirement Analysis

This service manages customer limit reduction plans using an event-driven architecture.

A reduction plan represents a customer request to reduce their account limit. For this small project, a plan contains the following core details:

- Account number
- Sort code
- Reduction amount
- Status, `PENDING`, `ACTIVE`

The key requirement is that the submitted plan should not be persisted directly by the API layer. Instead, the API publishes an event to Kafka. A Kafka consumer then consumes the event, processes the plan, and stores it in the database.

### Functional Requirements

The service must support the following capabilities:

1. Submit a reduction plan through a REST API.
2. Publish a reduction plan event to a Kafka topic after a valid submission.
3. Consume reduction plan events from Kafka.
4. Process and persist consumed reduction plans.
5. Retrieve the latest reduction plan for a given account.

### Non-Functional Requirements

The implementation should focus on:

- Clean and maintainable code.
- Clear separation of concerns between the REST API, Kafka producer, Kafka consumer, business logic, and persistence layer.
- Unit tests for core business logic.
- Integration tests for API and Kafka components.
- A simple local development setup.

## Business Assumptions

The original requirement is intentionally lightweight, so the following assumptions are made to keep the implementation clear and manageable.

### Account Identification

An account is identified by the combination of:

- `accountNumber`
- `sortCode`

Although the requirement mentions retrieving the latest plan for an account, this implementation treats `accountNumber + sortCode` as the account identifier. This is closer to how UK bank accounts are commonly represented and avoids relying on account number alone.

### Plan Submission

A submitted reduction plan is initially represented as a `PENDING` plan event.

The REST API does not directly persist the plan. Instead, it validates the request and publishes a Kafka event. Persistence happens asynchronously when the Kafka consumer processes the event.

Because processing is asynchronous, the submit endpoint returns `202 Accepted` rather than `201 Created`.

### Plan Status

The initial supported plan statuses are:

- `PENDING`
- `ACTIVE`

Assumption:

- A submitted plan is represented as a `PENDING` event.
- After the Kafka consumer successfully processes and stores the plan, the stored plan becomes `ACTIVE`.

A `FAILED` plan status is not included in the initial implementation.

This is intentional because not all failures should become plan statuses:

- If the request is invalid, for example the reduction amount is zero or negative, the request is rejected with `400 Bad Request`. No plan is created and no Kafka event is published.
- If publishing to Kafka fails, the API should return an error such as `503 Service Unavailable` or `500 Internal Server Error`. The request should not be reported as accepted because the asynchronous process has not started.
- If the Kafka consumer fails while processing an already-published event, the failure should be handled through retry logic and, optionally, a dead-letter topic.

In a more complete production system, `FAILED` could be introduced either as a plan status or as a separate event-processing status. For this assessment, keeping the plan statuses to `PENDING` and `ACTIVE` avoids mixing business state with technical processing failures.

### Latest Plan Definition

Multiple reduction plans may exist for the same account.

The latest plan is defined as the plan with the most recent `createdAt` timestamp for the same `accountNumber` and `sortCode`.

This avoids introducing additional business rules such as "only one active plan per account", which would require more domain detail than the assessment provides.

### Reduction Amount Validation

The reduction amount must be greater than zero.

This implementation does not validate the reduction amount against the customer's current limit because no current-limit source is provided in the assessment.

In a real banking system, the service would likely need to validate that:

- The account exists.
- The customer is authorised to request a reduction.
- The requested reduction amount does not exceed the current available limit.
- The resulting limit does not go below an allowed minimum.

### Eventual Consistency

The system is eventually consistent.

After a client submits a reduction plan, there may be a short delay before the plan can be retrieved through the query endpoint because the plan is stored only after Kafka consumption.

### Sensitive Data Handling

Account number and sort code are treated as sensitive data.

In a production system, these values should be masked in logs and protected according to the organisation's data-handling policies.

For this assessment, logging should avoid unnecessarily printing full account details.

## User Stories

### Story 1: Submit a Reduction Plan

As a client of the reduction plan service,  
I want to submit a reduction plan for an account,  
so that the customer's requested limit reduction can be processed.

#### Acceptance Criteria

Given a valid account number, sort code, and reduction amount,  
when the client submits a reduction plan,  
then the service should publish a reduction plan event to Kafka,  
and return `202 Accepted`.

Given the submitted request is invalid,  
when the client submits the request,  
then the service should return `400 Bad Request`,  
and the request should not be published to Kafka.

---

### Story 2: Process a Reduction Plan Event

As the reduction plan service,  
I want to consume reduction plan events from Kafka,  
so that submitted plans can be processed asynchronously.

#### Acceptance Criteria

Given a valid reduction plan event exists in Kafka,  
when the consumer receives the event,  
then the service should process the event,  
store the reduction plan in the database,  
and mark the stored plan as `ACTIVE`.

Given the event cannot be processed,  
when the consumer handles the event,  
then the failure should be logged and handled according to the configured retry strategy.

---

### Story 3: Retrieve the Latest Reduction Plan

As a client of the reduction plan service,  
I want to retrieve the latest reduction plan for an account,  
so that I can view the most recent reduction request for that account.

#### Acceptance Criteria

Given one or more reduction plans exist for an account,  
when the client requests the latest reduction plan,  
then the service should return the most recently created plan for the given account number and sort code.

Given no reduction plan exists for the account,  
when the client requests the latest reduction plan,  
then the service should return `404 Not Found`.

---

### Story 4: Prevent Duplicate Event Processing

As the reduction plan service,  
I want duplicate Kafka events to be ignored,  
so that the same reduction plan is not processed more than once.

#### Acceptance Criteria

Given an event has already been processed,  
when the same event is consumed again,  
then the service should not create a duplicate reduction plan.

This story is treated as a reliability improvement. The event model includes an `eventId` so that idempotency can be implemented cleanly.

---

### Story 5: Validate Business Input

As the reduction plan service,  
I want to validate submitted reduction plan requests,  
so that invalid or incomplete requests are rejected before an event is published.

#### Acceptance Criteria

The service should reject requests where:

- `accountNumber` is missing or invalid.
- `sortCode` is missing or invalid.
- `reductionAmount` is missing, zero, or negative.

Invalid requests should return `400 Bad Request`.

## Delivery Plan

The implementation is delivered in phases to keep the scope manageable while still leaving clear extension points for a more production-ready service.

### Phase 1: Core MVP

The goal of this phase is to implement the minimum end-to-end event-driven flow.

Scope:

- Create the Spring Boot project structure.
- Define request, response, event, domain, and persistence models.
- Implement `POST /api/reduction-plans`.
- Publish a reduction plan event to Kafka.
- Implement a Kafka consumer for reduction plan events.
- Persist consumed plans to a simple database.
- Implement `GET /api/reduction-plans/latest`.
- Add basic unit tests for service logic.

Expected outcome:

A client can submit a valid reduction plan, the service publishes an event, the consumer stores the plan, and the latest plan can be retrieved through the query API.

---

### Phase 2: Validation, Error Handling, and Test Coverage

The goal of this phase is to make the MVP more robust and easier to maintain.

Scope:

- Add request validation using Bean Validation.
- Add a global exception handler.
- Return consistent error responses.
- Add controller tests for REST API behaviour.
- Add service unit tests.
- Add integration tests for API and Kafka flow.
- Document the asynchronous behaviour and eventual consistency model.

Expected outcome:

The service rejects invalid input clearly, has predictable error handling, and includes tests that prove the main behaviours work.

---

### Phase 3: Event-Driven Reliability Improvements

The goal of this phase is to address common reliability concerns in event-driven systems.

Scope:

- Add `eventId` to the event model.
- Add `eventType`, `eventVersion`, and `occurredAt` to support event evolution.
- Implement idempotency handling to avoid duplicate processing.
- Add retry handling for Kafka consumer failures.
- Optionally add a dead-letter topic for messages that repeatedly fail.
- Improve logging while avoiding exposure of sensitive account details.

Expected outcome:

The service can handle duplicate events and consumer failures more safely, while keeping the architecture simple and understandable.

---

### Phase 4: Local Development and Documentation

The goal of this phase is to make the project easy to run, review, and discuss during the technical interview.

Scope:

- Add Docker Compose for running Kafka locally.
- Add clear local setup instructions.
- Add example curl commands for API testing.
- Document assumptions and trade-offs.
- Document future improvements.
- Ensure README explains the architecture and design decisions.

Expected outcome:

A reviewer can clone the repository, run the service locally, execute the tests, and understand the design decisions without needing additional explanation.
