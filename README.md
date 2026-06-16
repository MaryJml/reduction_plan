# Reduction Plan Service
Build a RESTful service that manages customer limit reduction plans using event-driven architecture.

## Requirement Analysis

This service manages customer limit reduction plans using an event-driven architecture.

A reduction plan represents a customer request to reduce their account limit. For this small project, a plan contains the following core details:

- Account number
- Sort code
- Reduction amount
- Status, `PENDING`, `ACTIVE`, `FAIL`

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

The initial supported statuses are:

- `PENDING`
- `ACTIVE`

Assumption:

- A plan is submitted as `PENDING`.
- After the Kafka consumer successfully processes and stores the plan, it becomes `ACTIVE`.

This keeps the state model simple while still showing the difference between request submission and asynchronous processing.

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


