# Evenlo

A backend API for an event booking platform, built with Spring Boot. The system handles user registration, event management, seat booking, and payment processing with Razorpay integration.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Sample Workflow](#sample-workflow)

## Features

- User authentication with JWT tokens
- Event lifecycle management (create, update, publish, delete)
- Seat inventory tracking with real-time availability
- Booking system with payment checkout flow
- Razorpay payment gateway integration with webhook verification
- Multi-tenant event administration (owners and admins)
- File upload and storage for event images
- Event analytics and summary reports
- Rate limiting with Redis-backed token bucket
- Asynchronous email notifications via Kafka
- Prometheus metrics for monitoring

## Tech Stack

- Java 21 with GraalVM support
- Spring Boot 3.2.2
- PostgreSQL 16 for data persistence
- Redis 7 for caching and rate limiting
- Apache Kafka for event-driven messaging
- Flyway for database migrations
- Lombok for boilerplate reduction
- SpringDoc OpenAPI for API documentation

## Project Structure

```
src/main/java/com/evenlo/
├── config/         # Security, Redis, and application configuration
├── controller/     # REST API endpoints
├── dto/            # Request and response data transfer objects
├── exception/      # Custom exception handlers
├── model/          # JPA entity classes
├── repository/     # Spring Data JPA repositories
├── service/        # Business logic layer
└── util/           # Helper utilities
```

**Controllers:**
- `AuthController` - Registration, login, and user profile
- `EventController` - Event CRUD and lifecycle operations
- `BookingController` - Seat booking and cancellation
- `PaymentController` - Payment verification via Razorpay
- `FileController` - File upload handling
- `AnalyticsController` - Event statistics and reports

**Models:**
- `User` - Platform users with roles
- `Event` - Event details with status tracking
- `SeatInventory` - Ticket tier and availability
- `Booking` - User reservations
- `Payment` - Payment records and status
- `StoredFile` - Uploaded file metadata
- `EventMembership` - Event-level admin assignments

## Prerequisites

Before you begin, make sure you have the following installed:

- GraalVM or any JDK 21 distribution
- Maven 3.8 or higher
- Docker and Docker Compose
- A Razorpay account for payment testing

## Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/evenlo.git
   cd evenlo
   ```

2. Copy the example environment file:
   ```
   cp .env.example .env
   ```

3. Update the `.env` file with your credentials:
   ```
   JWT_SECRET=your-long-random-secret-key
   RAZORPAY_KEY_ID=rzp_test_your_key_id
   RAZORPAY_KEY_SECRET=rzp_test_your_secret
   ```

## Configuration

The application is configured through environment variables. See `.env.example` for a complete list.

**Database:**
- `DB_URL` - PostgreSQL connection string
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password

**Security:**
- `JWT_SECRET` - Secret key for signing JWT tokens (required)
- `JWT_ISSUER` - Token issuer name
- `JWT_ACCESS_TTL_SECONDS` - Token expiration time

**Payment Gateway:**
- `RAZORPAY_KEY_ID` - Your Razorpay key ID (required)
- `RAZORPAY_KEY_SECRET` - Your Razorpay key secret (required)
- `RAZORPAY_BASE_URL` - Razorpay API base URL

**Rate Limiting:**
- `RATE_LIMIT_ENABLED` - Toggle rate limiting on or off
- `RATE_LIMIT_CAPACITY` - Maximum requests in the bucket
- `RATE_LIMIT_REFILL_TOKENS` - Tokens added per period
- `RATE_LIMIT_REFILL_PERIOD_SECONDS` - Refill interval

## Running the Application

1. Start the infrastructure services:
   ```
   docker compose up -d
   ```
   This brings up PostgreSQL, Redis, Kafka, and Mailhog.

2. Set the `JAVA_HOME` environment variable to point to your JDK 21 installation.

3. Run the application:
   ```
   mvn spring-boot:run
   ```

4. The API will be available at `http://localhost:8080`.

5. Open the Swagger UI documentation at:
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

6. Mailhog web interface for viewing sent emails:
   ```
   http://localhost:8025
   ```

## API Endpoints

### Authentication

| Method | Endpoint              | Description           |
|--------|----------------------|-----------------------|
| POST   | /api/v1/auth/register | Create a new account  |
| POST   | /api/v1/auth/login    | Obtain a JWT token    |
| GET    | /api/v1/auth/me       | Get current user info |

### Events

| Method | Endpoint                           | Description                  |
|--------|-----------------------------------|------------------------------|
| POST   | /api/v1/events                     | Create a new event           |
| GET    | /api/v1/events                     | List events with filters     |
| GET    | /api/v1/events/{id}                | Get event by ID              |
| PUT    | /api/v1/events/{id}                | Update event details         |
| POST   | /api/v1/events/{id}/publish        | Publish a draft event        |
| DELETE | /api/v1/events/{id}                | Delete an event              |
| POST   | /api/v1/events/{id}/admins/{userId}| Assign an event admin        |
| POST   | /api/v1/events/{id}/image/{fileId} | Set event cover image        |

### Bookings

| Method | Endpoint                   | Description            |
|--------|---------------------------|------------------------|
| POST   | /api/v1/bookings           | Create booking checkout|
| GET    | /api/v1/bookings           | List my bookings       |
| GET    | /api/v1/bookings/{id}      | Get booking details    |
| PUT    | /api/v1/bookings/{id}/cancel| Cancel a booking      |

### Payments

| Method | Endpoint                         | Description               |
|--------|----------------------------------|---------------------------|
| POST   | /api/v1/payments/razorpay/verify | Verify Razorpay payment   |

### Files

| Method | Endpoint       | Description      |
|--------|---------------|------------------|
| POST   | /api/v1/files  | Upload a file    |
| GET    | /api/v1/files/{id} | Download a file |

### Analytics

| Method | Endpoint                             | Description           |
|--------|-------------------------------------|-----------------------|
| GET    | /api/v1/analytics/events/{id}/summary| Event analytics report|

## Sample Workflow

Here is a typical flow for creating and booking an event:

1. Register a new user:
   ```
   POST /api/v1/auth/register
   {
     "email": "organizer@example.com",
     "password": "securepassword",
     "displayName": "Event Organizer"
   }
   ```

2. Log in to get a JWT token:
   ```
   POST /api/v1/auth/login
   {
     "email": "organizer@example.com",
     "password": "securepassword"
   }
   ```

3. Create a new event:
   ```
   POST /api/v1/events
   {
     "title": "Tech Conference 2025",
     "description": "Annual technology conference",
     "city": "Mumbai",
     "venueName": "Convention Center",
     "startsAt": "2025-06-15T09:00:00Z",
     "endsAt": "2025-06-15T18:00:00Z",
     "seatInventories": [
       {
         "tierName": "General",
         "totalSeats": 500,
         "priceInPaise": 50000
       },
       {
         "tierName": "VIP",
         "totalSeats": 50,
         "priceInPaise": 150000
       }
     ]
   }
   ```

4. Publish the event:
   ```
   POST /api/v1/events/{eventId}/publish
   ```

5. Book seats (as a different user):
   ```
   POST /api/v1/bookings
   {
     "eventId": "{eventId}",
     "seatInventoryId": "{seatInventoryId}",
     "quantity": 2
   }
   ```
   This returns a Razorpay order ID for payment.

6. After completing payment on the client, verify it:
   ```
   POST /api/v1/payments/razorpay/verify
   {
     "razorpayOrderId": "order_xxx",
     "razorpayPaymentId": "pay_xxx",
     "razorpaySignature": "signature_xxx"
   }
   ```

7. View event analytics:
   ```
   GET /api/v1/analytics/events/{eventId}/summary
   ```


