# User Service - Mental Health Forum

This service is a core component of the Mental Health Forum application. It is responsible for all user-related operations, including user profiles, registration, and authentication.

---

## Tech Stack

* **Java 21**
* **Spring Boot 3.x.x**
* **Maven**
* **Docker**

---

## Prerequisites

Before you begin, ensure you have the following installed on your local machine:
* JDK 21
* Apache Maven
* Docker Desktop

---

## How to Run

You can run the service in two ways: locally for development or as a Docker container.

### 1. Running Locally

This method is ideal for active development and debugging.

1.  Navigate to the service's root directory (`/backend/user-service`).
2.  Run the application using the Maven wrapper:
    ```bash
    ./mvnw spring-boot:run
    ```
3.  The service will be available at `http://localhost:8080`.

### 2. Running with Docker

This is the standard way to run the service in a production-like environment.

1.  **Build the Docker Image:**
    Navigate to the service's root directory and run the build command. Remember to update the version tag as you make changes.
    ```bash
    docker build -t mental-health-forum/user-service:0.0.2 .
    ```

2.  **Run the Docker Container:**
    Once the image is built, run the following command to start the container.
    ```bash
    docker run -p 8080:8080 -d --name user-service mental-health-forum/user-service:0.0.2
    ```
    * `-p 8080:8080` maps the port from your local machine to the container.
    * `-d` runs the container in detached mode.
    * `--name user-service` gives the running container a convenient name.

---

## API Endpoints

The following are the available API endpoints for this service.

| Method | Endpoint        | Description                          |
| ------ | --------------- | ------------------------------------ |
| `GET`  | `/api/hello`    | A simple health-check/test endpoint. |

*(This section will be updated as more endpoints are added.)*

---

## Configuration

Application configuration is managed in `src/main/resources/application.properties`.

*(This section will be updated to include environment variables for database connections, etc.)*