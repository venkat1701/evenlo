# Evenlo Backend

Spring Boot backend for an Event Booking Platform.

## Local Infra

Run Postgres + Redis + Kafka + Mailhog:

`docker compose up -d`

## Required environment variables

- `JWT_SECRET`
- `RAZORPAY_KEY_ID`
- `RAZORPAY_KEY_SECRET`

See `.env.example` for the full list.

## Java / GraalVM

Maven requires `JAVA_HOME` to point to a JDK.

- Install GraalVM (Java 21)
- Set `JAVA_HOME` to that installation directory

## Run

- Start infra: `docker compose up -d`
- Start app: `mvn spring-boot:run`

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Demo flow

- Register: `POST /api/v1/auth/register`
- Login: `POST /api/v1/auth/login`
- Create event: `POST /api/v1/events`
- Publish event: `POST /api/v1/events/{id}/publish`
- Book seats + get Razorpay order id: `POST /api/v1/bookings`
- Verify payment: `POST /api/v1/payments/razorpay/verify`
- Analytics: `GET /api/v1/analytics/events/{eventId}/summary`

Optional overrides are in `application.yml`.
