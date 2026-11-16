# RentVideo API

A RESTful API built with **Spring Boot 3** for managing an online video rental system.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Security | Spring Security 6 — Basic Auth |
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
Hibernate creates the `users` and `videos` tables automatically on first run.

### 5. Run tests
```bash
mvn test
```

---

## Authentication

The API uses **HTTP Basic Auth**.  
Add an `Authorization: Basic <base64(email:password)>` header to every private request.

| Role | Permissions |
|---|---|
| `CUSTOMER` | Browse videos, view own profile |
| `ADMIN` | Everything a CUSTOMER can do + create/update/delete videos |

---

## API Endpoints

### Auth

| Method | Endpoint | Auth required | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | No | Register a new user |
| `GET` | `/api/auth/me` | Yes | Get current user profile |

### Videos

| Method | Endpoint | Auth required | Role | Description |
|---|---|---|---|---|
| `GET` | `/api/videos` | Yes | Any | List all videos |
| `GET` | `/api/videos/{id}` | Yes | Any | Get a video by ID |
| `POST` | `/api/videos` | Yes | ADMIN | Create a new video |
| `PUT` | `/api/videos/{id}` | Yes | ADMIN | Update a video |
| `DELETE` | `/api/videos/{id}` | Yes | ADMIN | Delete a video |

---

## Sample Requests

### Register a customer
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "secret123",
  "firstName": "Alice",
  "lastName": "Smith",
  "role": "CUSTOMER"
}
```
> `role` defaults to `CUSTOMER` if omitted.

### Register an admin
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "adminpass",
  "firstName": "Admin",
  "lastName": "User",
  "role": "ADMIN"
}
```

### Create a video (ADMIN)
```http
POST /api/videos
Authorization: Basic YWRtaW5AZXhhbXBsZS5jb206YWRtaW5wYXNz
Content-Type: application/json

{
  "title": "The Matrix",
  "director": "Wachowski Sisters",
  "genre": "Sci-Fi"
}
```

### List all videos (any authenticated user)
```http
GET /api/videos
Authorization: Basic YWxpY2VAZXhhbXBsZS5jb206c2VjcmV0MTIz
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
| `400` | Duplicate email or bad request |
| `401` | Missing or wrong credentials |
| `403` | Authenticated but insufficient role |
| `404` | Resource not found |

---

## Project Structure

```
src/main/java/com/rentvideo/
├── RentVideoApplication.java
├── config/
│   ├── SecurityConfig.java          # Basic Auth + authorization rules
│   └── UserDetailsServiceImpl.java  # Loads user from DB for Spring Security
├── controller/
│   ├── AuthController.java          # /api/auth/*
│   └── VideoController.java         # /api/videos/*
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
