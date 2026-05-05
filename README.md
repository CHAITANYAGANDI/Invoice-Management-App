# Invoice Management API

A production-style Spring Boot REST API for managing clients, invoices, invoice items, payments, invoice status, filtering, pagination, sorting, validation, database migrations, Swagger documentation, and automated tests.

This project was built as a backend portfolio project to practice real-world Spring Boot application structure, service-layer business logic, DTO-based API design, Flyway migrations, and multi-level testing.

---

## Tech Stack

- Java 21
- Spring Boot 3.5.6
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Bean Validation
- Swagger / OpenAPI
- JUnit 5
- Mockito
- MockMvc
- H2 Database for repository slice tests
- Testcontainers with PostgreSQL for integration tests
- Maven

---

## Main Features

### Client Management

- Create a client
- Get all clients
- Get client by ID
- Update client by ID
- Delete client by ID
- Search clients by name
- Validate client request data
- Handle client not found errors

### Invoice Management

- Create invoice for an existing client
- Get all invoices
- Get invoice by ID
- Update invoice by ID
- Delete invoice by ID
- Get invoices by client ID
- Get invoices by status
- Update invoice status
- Filter invoices by status, client ID, and issue date range
- Pagination and sorting for invoice filters

### Invoice Item Management

- Add multiple invoice items while creating or updating an invoice
- Calculate each invoice item line total automatically
- Replace invoice items during invoice update
- Maintain invoice-to-item relationship using JPA cascading

### Payment Logic

- Update amount paid for an invoice
- Recalculate balance due automatically
- Prevent negative payment amounts
- Prevent overpayment
- Automatically update invoice status:
  - `PARTIALLY_PAID` when partial amount is paid
  - `PAID` when full amount is paid
  - `SENT` when paid amount is reset to zero after being paid or partially paid

### Business Calculations

The API calculates invoice totals in the service layer.

```text
lineTotal = quantity * unitPrice
subtotal = sum of all line totals
taxAmount = subtotal * taxRate / 100
discountAmount = subtotal * discountRate / 100
totalAmount = subtotal + taxAmount - discountAmount
balanceDue = totalAmount - amountPaid
```

The user does not send calculated fields like subtotal, tax amount, total amount, line total, or balance due. These values are calculated by the backend.

---

## Project Structure

```text
src/main/java/org/example/invoicemanagement
│
├── config
│   ├── JpaAuditingConfig.java
│   └── OpenApiConfig.java
│
├── controller
│   ├── ClientController.java
│   └── InvoiceController.java
│
├── dto
│   ├── ClientRequestDTO.java
│   ├── ClientResponseDTO.java
│   ├── InvoiceItemRequestDTO.java
│   ├── InvoiceItemResponseDTO.java
│   ├── InvoiceRequestDTO.java
│   └── InvoiceResponseDTO.java
│
├── entity
│   ├── Client.java
│   ├── Invoice.java
│   └── InvoiceItem.java
│
├── enums
│   └── InvoiceStatus.java
│
├── exception
│   ├── ClientNotFoundException.java
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   └── InvoiceNotFoundException.java
│
├── repository
│   ├── ClientRepository.java
│   ├── InvoiceItemRepository.java
│   └── InvoiceRepository.java
│
├── service
│   ├── ClientService.java
│   └── InvoiceService.java
│
├── specification
│   └── InvoiceSpecification.java
│
└── InvoiceManagementApplication.java
```

---

## Entity Relationships

```text
Client 1 ──── * Invoice
Invoice 1 ──── * InvoiceItem
```

- One client can have many invoices.
- One invoice can have many invoice items.
- Invoice items are saved and deleted through the invoice using JPA cascade and orphan removal.

---

## Invoice Status Values

```text
DRAFT
SENT
PAID
PARTIALLY_PAID
OVERDUE
CANCELLED
```

---

## API Endpoints

Base URL:

```text
http://localhost:8080
```

If your application runs on another port, replace `8080` with your configured port.

---

## Client Endpoints

### Create Client

```http
POST /api/v1/clients
```

Request body:

```json
{
  "name": "Sai Technologies",
  "email": "sai@example.com",
  "phone": "+1 437 123 4567",
  "billingAddress": "Toronto, ON"
}
```

---

### Get All Clients

```http
GET /api/v1/clients
```

---

### Get Client by ID

```http
GET /api/v1/clients/{id}
```

---

### Update Client by ID

```http
PUT /api/v1/clients/{id}
```

Request body:

```json
{
  "name": "Updated Client",
  "email": "updated@example.com",
  "phone": "+1 416 111 2222",
  "billingAddress": "Markham, ON"
}
```

---

### Delete Client by ID

```http
DELETE /api/v1/clients/{id}
```

---

### Search Clients by Name

```http
GET /api/v1/clients/search?name=sai
```

---

## Invoice Endpoints

### Create Invoice

```http
POST /api/v1/invoices
```

Request body:

```json
{
  "clientId": 1,
  "issueDate": "2030-05-05",
  "dueDate": "2030-05-20",
  "taxRate": 13.00,
  "discountRate": 5.00,
  "items": [
    {
      "description": "Website development",
      "quantity": 2,
      "unitPrice": 100.00
    }
  ]
}
```

Example calculated response values:

```json
{
  "subTotal": 200.00,
  "taxAmount": 26.00,
  "discountAmount": 10.00,
  "totalAmount": 216.00,
  "amountPaid": 0.00,
  "balanceDue": 216.00,
  "status": "DRAFT"
}
```

---

### Get All Invoices

```http
GET /api/v1/invoices
```

---

### Get Invoice by ID

```http
GET /api/v1/invoices/{id}
```

---

### Update Invoice by ID

```http
PUT /api/v1/invoices/{id}
```

This replaces invoice details and invoice items, then recalculates invoice totals.

Request body:

```json
{
  "clientId": 1,
  "issueDate": "2030-05-05",
  "dueDate": "2030-05-25",
  "taxRate": 13.00,
  "discountRate": 0.00,
  "items": [
    {
      "description": "Backend API development",
      "quantity": 1,
      "unitPrice": 500.00
    },
    {
      "description": "Database setup",
      "quantity": 1,
      "unitPrice": 150.00
    }
  ]
}
```

---

### Delete Invoice by ID

```http
DELETE /api/v1/invoices/{id}
```

---

### Get Invoices by Client ID

```http
GET /api/v1/invoices/client/{clientId}
```

---

### Get Invoices by Status

```http
GET /api/v1/invoices/status/{status}
```

Example:

```http
GET /api/v1/invoices/status/PAID
```

---

### Update Invoice Status

```http
PATCH /api/v1/invoices/{id}/status?status=SENT
```

---

### Update Invoice Payment

```http
PATCH /api/v1/invoices/{id}/payment?amountPaid=100.00
```

Payment update behavior:

```text
amountPaid = 0                  -> balance due remains full amount
amountPaid > 0 and < total      -> status becomes PARTIALLY_PAID
amountPaid == total             -> status becomes PAID
amountPaid > total              -> returns 400 Bad Request
amountPaid < 0                  -> returns 400 Bad Request
```

---

### Filter Invoices with Pagination and Sorting

```http
GET /api/v1/invoices/filter
```

Supported query parameters:

```text
status
clientId
fromDate
toDate
page
size
sortBy
sortDir
```

Example:

```http
GET /api/v1/invoices/filter?status=PAID&clientId=1&fromDate=2030-05-01&toDate=2030-05-31&page=0&size=5&sortBy=dueDate&sortDir=desc
```

Allowed sort fields:

```text
id
invoiceNumber
issueDate
dueDate
status
totalAmount
balanceDue
createdAt
```

---

## Validation Rules

### Client Validation

- Client name is required.
- Email is required.
- Email must be valid.
- Client name must be at most 100 characters.
- Email must be at most 150 characters.
- Phone number must be at most 20 characters.
- Billing address must be at most 255 characters.

### Invoice Validation

- Client ID is required.
- Issue date is required.
- Due date is required.
- Due date must be equal to or after issue date.
- Tax rate cannot be negative.
- Discount rate cannot be negative.
- Invoice must have at least one item.

### Invoice Item Validation

- Item description is required.
- Item description must be at most 255 characters.
- Quantity must be at least 1.
- Unit price must be greater than 0.

### Payment Validation

- Amount paid is required.
- Amount paid cannot be negative.
- Amount paid cannot be greater than total amount.

---

## Error Response Format

For not found or business validation errors, the API returns a structured error response.

Example:

```json
{
  "timestamp": "2026-05-05T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Invoice not found with id: 100",
  "path": "/api/v1/invoices/100"
}
```

Validation errors return field-level messages.

Example:

```json
{
  "email": "Email should be valid",
  "name": "Client name is required"
}
```

---

## Database Migrations with Flyway

Flyway is used to manage database schema changes.

Migration location:

```text
src/main/resources/db/migration
```

Initial migration file:

```text
V1__create_initial_invoice_schema.sql
```

Flyway creates:

```text
client
invoice
invoice_item
flyway_schema_history
```

Hibernate should validate the schema instead of creating tables automatically.

Recommended setting:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

---

## Swagger / OpenAPI

Swagger UI is available after starting the application.

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

---

## Running the Application Locally

### Prerequisites

- Java 21
- Maven
- PostgreSQL
- A PostgreSQL database created locally

Example database name:

```text
invoice_management_db
```

### Example `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/invoice_management_db
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.flyway.enabled=true

springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
```

### Start the application

```bash
mvn spring-boot:run
```

---

## Running Tests

Run all tests:

```bash
mvn test
```

Run service tests:

```bash
mvn test -Dtest=ClientServiceTest,InvoiceServiceTest
```

Run controller tests:

```bash
mvn test -Dtest=ClientControllerTest,InvoiceControllerTest
```

Run repository tests:

```bash
mvn test -Dtest=ClientRepositoryTest,InvoiceRepositoryTest
```

Run integration tests:

```bash
mvn test -Dtest=InvoiceManagementIntegrationTest
```

Integration tests use Testcontainers with PostgreSQL. Docker Desktop must be running before running the integration tests.

---

## Testing Strategy

This project includes four levels of testing.

### Level 1: Service Unit Tests

Test business logic using JUnit and Mockito.

Covered examples:

- Client creation
- Client lookup
- Client update
- Client deletion
- Invoice creation calculations
- Invoice payment update
- Invoice status updates
- Overpayment validation

### Level 2: Controller Tests

Test REST endpoints using `@WebMvcTest` and MockMvc.

Covered examples:

- HTTP methods
- URL mappings
- Request bodies
- Query parameters
- Status codes
- Response JSON
- Validation errors
- Exception handling

### Level 3: Repository Tests

Test JPA repository methods using `@DataJpaTest` and H2.

Covered examples:

- Save client
- Search client by name
- Save invoice with items
- Find invoices by client ID
- Find invoices by status
- Find invoice by invoice number
- Specification filtering

### Level 4: Integration Tests

Test full backend flow with Spring Boot, MockMvc, Testcontainers, and PostgreSQL.

Covered examples:

- Create client
- Create invoice
- Get invoice by ID
- Update payment
- Filter invoices
- Delete invoice
- Validate business errors

---

## Completed Test Classes

```text
ClientServiceTest
InvoiceServiceTest
ClientControllerTest
InvoiceControllerTest
ClientRepositoryTest
InvoiceRepositoryTest
InvoiceManagementIntegrationTest
```

---

## Example Invoice Calculation

Input:

```json
{
  "taxRate": 13.00,
  "discountRate": 5.00,
  "items": [
    {
      "description": "Website development",
      "quantity": 2,
      "unitPrice": 100.00
    }
  ]
}
```

Calculation:

```text
lineTotal = 2 * 100.00 = 200.00
subtotal = 200.00
taxAmount = 200.00 * 13 / 100 = 26.00
discountAmount = 200.00 * 5 / 100 = 10.00
totalAmount = 200.00 + 26.00 - 10.00 = 216.00
amountPaid = 0.00
balanceDue = 216.00
status = DRAFT
```


## Project Summary

This project demonstrates a clean Spring Boot backend with:

- Layered architecture
- DTO-based request and response handling
- JPA entity relationships
- Real business calculations
- Centralized exception handling
- Bean validation
- Flyway database migrations
- Swagger API documentation
- Unit, controller, repository, and integration testing

It is a strong backend project and a foundation for building a larger production-style monolith application.
