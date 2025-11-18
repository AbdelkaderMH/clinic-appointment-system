# Deployment Guide 🚀

Comprehensive deployment guide for the Clinic Appointment System with multiple deployment options.

## Deployment Options

The application supports three deployment strategies:

1. **Local Deployment** - Direct JVM execution for development
2. **Docker Deployment** - Containerized deployment (recommended)
3. **Kubernetes Deployment** - Production-ready orchestration with monitoring

## Option 1: Local Deployment

### Prerequisites
- **Java 21+**
- **Maven 3.6+** 
- **MySQL 8.0** running on localhost:3306

### Quick Start

```bash
# Clone and build
git clone https://github.com/AbdelkaderMH/clinic-appointment-system.git
cd clinic-appointment-system
mvn clean install
mvn spring-boot:run
```

### Using Build Scripts

```bash
# Build the application
./scripts/build.sh

# Start the application
./scripts/run.sh
```

### Manual Build (Alternative)

1. Install Java 21+ and Maven 3.6+
2. Build the project:

   ```bash
   mvn clean package -DskipTests
   ```

3. Run the JAR file:

   ```bash
   java -jar target/clinic-appointment-system-*.jar
   ```

**Application URL**: `http://localhost:8090`

## Option 2: Docker Deployment

### Docker Compose Setup (Recommended)

Use the provided `docker-compose.yml` to bring up the complete stack with
MySQL database, application, and reverse proxy:

```bash
cd docker
docker-compose up --build
```

This starts:
- **MySQL 8.0** on port 3306 (internal networking only)
- **Application** on port 8090
- **Nginx** reverse proxy on port 80
- **Persistent volume** for MySQL data

To stop:
```bash
docker-compose down
```

To view logs:
```bash
docker-compose logs -f
```

### Alternative: Build and Run Manually

If you prefer to build and run the image separately, you have two options:

#### Option A: With External MySQL (Advanced)

Build from docker folder:
```bash
cd docker
docker build -t clinic-appointment-system:latest -f Dockerfile ..
docker run -p 8090:8090 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/clinic_db \
  clinic-appointment-system:latest
```

Or build from root directory:
```bash
docker build -t clinic-appointment-system:latest -f docker/Dockerfile .
docker run -p 8090:8090 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/clinic_db \
  clinic-appointment-system:latest
```

**Requirements:**
- MySQL 8.0 running on your host machine (port 3306)
- `host.docker.internal` refers to host machine from inside container
- Database: `clinic_db`
- User: `clinic_user` / Password: `clinic_password`

#### Option B: Recommended - Use Docker Compose (Easier)

**⭐ Recommended:** Use `docker-compose up --build` instead. It automatically:
- Starts MySQL 8.0 container
- Starts Application container
- Sets up networking
- Manages dependencies and health checks
- Persists data with volumes

This is the simplest and most reliable approach for local development and testing.

## Configuration

### Local Development

The application uses `application.properties` with the following defaults:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/clinic_db
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
```

Ensure MySQL is running locally on port 3306.

### Docker Environment

When running with docker-compose, the app automatically loads
`application-docker.properties`:

```properties
spring.datasource.url=jdbc:mysql://mysql:3306/clinic_db
spring.datasource.username=clinic_user
spring.datasource.password=clinic_password
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

### Production Environment

For production, use `application-prod.properties`:

```properties
spring.datasource.url=jdbc:mysql://prod-db-host:3306/clinic_prod
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
```

Set environment variables: `DB_USER` and `DB_PASSWORD`.

## Accessing the Application

- **API Endpoint**: `http://localhost:8090/api/`
- **Swagger UI**: `http://localhost:8090/swagger-ui/index.html`
- **Health Check**: `http://localhost:8090/actuator/health`

## Option 3: Kubernetes Deployment with Monitoring

### Minikube Cluster Management

```bash
# Start Minikube cluster
minikube start

# Check cluster status
minikube status

# Delete cluster
minikube delete
```

### Jenkins CI/CD Pipeline

Run the complete Jenkins pipeline that includes:
1. **Environment Validation** - Validates required environment variables
2. **Build & Unit Tests** - Maven build with JUnit tests and JaCoCo coverage
3. **SonarQube Analysis** - Code quality analysis
4. **Docker Image Build** - Creates containerized application
5. **Kubernetes Deployment** - Deploys to Minikube cluster
6. **Monitoring Setup** - Prometheus and Grafana stack

```bash
# Run monitoring script
powershell .\minikubemonitoring.ps1
```

### Access Points

- **Application**: `http://localhost:8090`
- **Swagger UI**: `http://localhost:8090/swagger-ui/index.html`
- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3000` (admin/admin)
- **SonarQube**: `http://localhost:9000`

### Scaling

```bash
# Scale to 3 replicas
kubectl scale deployment clinic-appointment-system --replicas=3 -n clinic

# Verify scaling
kubectl get pods -n clinic
```

## Deployment Checklist

- [ ] Java 21+ installed (for local deployment)
- [ ] Docker and Docker Compose installed
- [ ] MySQL 8.0 configured (or use docker-compose)
- [ ] Application built successfully
- [ ] All tests passing
- [ ] Environment variables configured
- [ ] Health checks passing
- [ ] API endpoints accessible
- [ ] Data persistence verified
- [ ] Backups configured