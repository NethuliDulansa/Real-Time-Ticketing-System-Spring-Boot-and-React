# Ticketing System API Documentation

## Table of Contents

- Setup Instructions
- API Usage Guidelines
- Troubleshooting

---

## Setup Instructions

### Prerequisites

- **Java**: Ensure you have Java 11 or higher installed. You can download it from [Oracle](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) or use [OpenJDK](https://openjdk.java.net/).
- **Maven**: This project uses Maven for dependency management. Install Maven from [here](https://maven.apache.org/install.html) if not already installed.
- **Git**: To clone the repository, install Git from [here](https://git-scm.com/downloads).

### Installation Steps

1. **Clone the Repository**

   ```sh
   git clone https://github.com/your-repo/ticketing-system.git
   cd ticketing-system
   ```

2. **Build the Project**

   Use Maven Wrapper to build the project:

   ```sh
   ./mvnw clean install
   ```

   This will install all necessary dependencies and compile the project.

3. **Run the Application**

   Start the application using Maven Wrapper:

   ```sh
   ./mvnw spring-boot:run
   ```

   The application will start on `http://localhost:8080`.

4. **Access the Application**

   Open your browser and navigate to `http://localhost:8080` to access the Ticketing System.

### Configuration

- **Application Properties**
  
  Configuration settings are located in `application.properties` . You can modify database connections, server ports, and other settings here.

- **Environment Variables**
  
  You can set environment variables for sensitive information like database passwords. Ensure these are set before running the application.

## Troubleshooting

### Common Issues

#### 1. Application Fails to Start

- **Symptom:** Unable to start the application; errors in the console.
- **Possible Causes:**
  - Port `8080` is already in use.
  - Missing environment variables.
  - Database connection issues.
- **Solutions:**
  - **Port Conflict:**
    - Change the server port in `application.properties`:

      ```properties
      server.port=9090
      ```

    - Or stop the application using port `8080`.
  - **Environment Variables:**
    - Ensure all required environment variables are set.
  - **Database Connection:**
    - Verify database is running and connection details are correct.

#### 2. Database Connection Failed

- **Symptom:** Error messages related to database connectivity.
- **Possible Causes:**
  - Incorrect database URL, username, or password.
  - Database service is not running.
- **Solutions:**
  - Check and update database configurations in `application.properties`.
  - Ensure the database service is up and accessible.

#### 3. API Returns 500 Internal Server Error

- **Symptom:** Receiving `500` status code when calling API endpoints.
- **Possible Causes:**
  - Unhandled exceptions in the code.
  - Misconfigured dependencies.
- **Solutions:**
  - Check application logs for detailed error messages.
  - Review recent code changes for potential issues.
  - Ensure all dependencies are correctly installed using:

    ```sh
    ./mvnw clean install
    ```

### Logging

The application uses SLF4J for logging. Logs are output to the console. To adjust logging levels, modify the `logback.xml` file.

### Additional Resources

- **Documentation:** Refer to the `HELP.md`file for more detailed guidance.
- **Support:** Contact the development team at [support@ticketsystem.com](mailto:support@ticketsystem.com).

---

For further assistance, please refer to the source files in the repository:

- **Build Configuration:** `pom.xml`

- **Application Properties:** `application.properties`

- **Main Application Code:** `src/main/java/com/example/ticketing/`

- **Static Assets:** `static`
