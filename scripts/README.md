# Scripts Directory 📜

Utility scripts for local build and deployment of the Clinic Appointment System.

## Available Scripts

### `build.sh` - Complete Build Pipeline
Comprehensive build script that performs:
- **Clean**: Removes previous build artifacts (`mvn clean`)
- **Test**: Executes unit and integration tests (`mvn test`)
- **Coverage**: Generates JaCoCo code coverage reports
- **Package**: Creates executable JAR file (`mvn package`)
- **Docker**: Builds Docker image if Docker is available
- **Error handling**: Stops on any failure with clear error messages

```bash
./scripts/build.sh
```

**Output artifacts:**
- `target/clinic-appointment-system-*.jar` - Executable JAR
- `target/site/jacoco/index.html` - Coverage report
- `clinic-appointment-system:latest` - Docker image

### `run.sh` - Application Startup
Starts the Spring Boot application in development mode:
- Uses `mvn spring-boot:run` for hot reload capability
- Automatically loads `application.properties` configuration
- Connects to MySQL database on localhost:3306
- Enables debug logging for development

```bash
./scripts/run.sh
```

Application will be available at `http://localhost:8090`

### `test.sh` - Test Suite Execution
Executes the complete test suite:
- Runs all JUnit 5 unit tests
- Executes Spring Boot integration tests
- Uses H2 in-memory database for testing
- Displays test results and coverage summary

```bash
./scripts/test.sh
```

## Prerequisites

- **Java 21+**
- **Maven 3.6+**
- **MySQL 8.0** (running on localhost:3306)

## Quick Local Deployment

```bash
# 1. Build the application
./scripts/build.sh

# 2. Start the application
./scripts/run.sh
```

## Windows Users

Use Git Bash or WSL to run these shell scripts, or execute the Maven commands directly:

```cmd
mvn clean package
mvn spring-boot:run
```