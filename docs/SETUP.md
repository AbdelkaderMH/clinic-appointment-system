# Setup Instructions

1. Ensure you have Java 17 installed. Verify with `java -version`.
2. Install Maven 3.6 or newer. Verify with `mvn -version`.
3. Clone the repository:

   ```bash
   git clone https://github.com/yourusername/clinic-appointment-system.git
   cd clinic-appointment-system
   ```

4. Build and run the application:

   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. Access the API at `http://localhost:8080/api` and the Swagger UI at
   `http://localhost:8080/swagger-ui.html`.