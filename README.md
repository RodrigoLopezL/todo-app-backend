# Todo List App Backend

This is the backend for a Todo List application. It provides RESTful APIs for managing tasks, including creating, updating, deleting, and retrieving tasks. The backend is built using Spring Boot and includes an in-memory task repository for data storage.

## Features

- Create, update, delete, and retrieve tasks.
- Filter tasks by state, priority, and text.
- Calculate average completion times for tasks.
- Mark tasks as done or undone.
- Pagination and sorting support for task retrieval.

## Technologies Used

- **Java 21**
- **Spring Boot 3.4.4**
  - Spring Web
  - Spring Data JPA
- **JUnit 5** for testing
- **Mockito** for mocking in tests

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.9.9 or higher

### Running the Application

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/todo-app-backend.git
   cd todo-app-backend
   ```
2. Build the project:
   ```bash
     ./mvnw clean install
   ```
3.Run the application:
  ```bash
     ./mvnw spring-boot:run
   ```
4.The application will start on port 9090. You can access the APIs at http://localhost:9090.

## API Endpoints
# Task Management
- GET /todos : Retrieve all tasks with optional filters for state, priority, and text. Supports pagination and sorting.
- GET /todos/time : Retrieve average completion times for tasks.
- POST /todos : Create a new task.
- PUT /todos/{id} : Update an existing task.
- PATCH /todos/{id}/done :Mark a task as done.
- PATCH /todos/{id}/undone : Mark a task as undone.
- DELETE /todos/{id} : Delete a task by ID.

## Example Request
# Create a Task
  ```json
  POST /todos
  Content-Type: application/json
  
  {
  "text": "Finish project",
  "dueDate": "2025-04-20T12:00:00",
  "priority": "HIGH"
  }
  ```

# Response
  ```json
  {
    "id": 1,
    "text": "Finish project",
    "creationDate": "2025-04-18T10:00:00",
    "dueDate": "2025-04-20T12:00:00",
    "priority": "HIGH",
    "state": false
  }
  ```
## Test
# Running Tests
To run the tests, execute:
  ```bash
    ./mvnw test
  ```

## Configuration
The application runs on port 9090 by default. You can change this in src/main/resources/application.properties:
  ```java
    server.port=9090
  ```
