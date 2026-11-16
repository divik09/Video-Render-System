# RentVideo API

A RESTful API built with **Spring Boot 3** for managing an online video rental system.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Security | Spring Security 6 — JWT (jjwt) + Basic login |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| Build tool | Maven |
| Java | 17 |

---

## Getting Started

### 1. Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8 running locally

### 2. Create the database
```sql
CREATE DATABASE rentvideo_db;
```

### 3. Configure credentials
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

### 4. Run the app
```bash
mvn spring-boot:run
```

The server starts at `http://localhost:8080`.  
Hibernate creates the `users`, `videos` and `rentals` tables automatically on first run.

### 5. Run tests
```bash
mvn test
```

---

## Authentication

Stateless authentication with **JWT**:

1. Register via `POST /auth/register` (public).
2. Log in via `POST /auth/login` using **Basic Auth** (`email:password`). The response contains a JWT.
3. Send `Authorization: Bearer <token>` on every private request.

(HTTP Basic is also accepted on private endpoints for convenience.) Passwords are hashed with BCrypt.

| Role | Permissions |
|---|---|
| `CUSTOMER` | Browse available videos, rent/return videos, view own rentals/profile |
| `ADMIN` | Everything a CUSTOMER can do + create/update/delete videos |

Design choices: both roles may rent videos; a user may hold at most **2 active rentals**; a rented video
becomes unavailable until returned (row locks prevent double-renting under concurrency).

---

## API Endpoints

| Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|
| `POST` | `/auth/register` | No | - | Register (role defaults to CUSTOMER) |
| `POST` | `/auth/login` | Basic | Any | Returns a JWT |
| `GET` | `/auth/me` | Yes | Any | Current user profile |
| `GET` | `/videos` | Yes | Any | List videos available for rent |
| `GET` | `/videos/{id}` | Yes | Any | Get a video |
| `POST` | `/videos` | Yes | ADMIN | Create a video |
| `PUT` | `/videos/{id}` | Yes | ADMIN | Update a video |
| `DELETE` | `/videos/{id}` | Yes | ADMIN | Delete a video |
| `POST` | `/videos/{videoId}/rent` | Yes | Any | Rent a video |
| `POST` | `/videos/{videoId}/return` | Yes | Any | Return a rented video |
| `GET` | `/rentals` | Yes | Any | My active rentals |

### Sample requests
```http
POST /auth/register
{ "email": "alice@example.com", "password": "secret123", "firstName": "Alice", "lastName": "Smith" }

POST /auth/login            (Basic Auth alice@example.com:secret123)  ->  { "token": "...", "tokenType": "Bearer", ... }

POST /videos/1/rent         (Authorization: Bearer <token>)
```

---

## Error Responses

All errors follow a consistent JSON shape:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Video not found with id: 99"
}
```

| HTTP Code | When |
|---|---|
| `400` | Validation failure, duplicate email or malformed request |
| `401` | Missing or wrong credentials |
| `403` | Authenticated but insufficient role |
| `404` | Resource not found (video, or no active rental to return) |
| `409` | Video unavailable, or user already has 2 active rentals |

---

## Project Structure

```
src/main/java/com/rentvideo/
├── RentVideoApplication.java
├── config/
│   ├── SecurityConfig.java          # Basic Auth + authorization rules
│   └── UserDetailsServiceImpl.java  # Loads user from DB for Spring Security
├── controller/
│   ├── RentalController.java        # /videos/{id}/rent|return, /rentals
│   ├── AuthController.java          # /auth/*
│   └── VideoController.java         # /videos/*
├── dto/
│   ├── RegisterRequest.java
│   ├── UserResponse.java
│   ├── VideoRequest.java
│   └── VideoResponse.java
├── entity/
│   ├── Role.java                    # CUSTOMER | ADMIN
│   ├── User.java
│   └── Video.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── repository/
│   ├── UserRepository.java
│   └── VideoRepository.java
└── service/
    ├── UserService.java
    └── VideoService.java
```
