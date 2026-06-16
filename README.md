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


