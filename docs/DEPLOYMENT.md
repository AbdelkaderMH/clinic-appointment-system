# Deployment Guide

This application can be deployed either directly on a JVM or within a
Docker container.

## Local Deployment

1. Install Java 17 and Maven.
2. Build the project:

   ```bash
   mvn clean package -DskipTests
   ```

3. Run the JAR file:

   ```bash
   java -jar target/clinic-appointment-system-1.0.0.jar
   ```

## Docker Deployment

### Recommended: Docker Compose (Includes MySQL)

Use the provided `docker-compose.yml` to bring up the complete stack with
MySQL database, application, and reverse proxy:

```bash
cd docker
docker-compose up --build
```

This starts:
- **MySQL 8.0** on port 3306 (internal networking only)
- **Application** on port 8080
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
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/clinic_db \
  clinic-appointment-system:latest
```

Or build from root directory:
```bash
docker build -t clinic-appointment-system:latest -f docker/Dockerfile .
docker run -p 8080:8080 \
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

## Docker Folder Structure

The `docker/` folder contains all Docker-related files:

```
docker/
├── Dockerfile              # Application container image
├── docker-compose.yml      # Multi-service orchestration (MySQL + App + Nginx)
├── nginx.conf              # Nginx reverse proxy configuration
└── mysql-data/             # MySQL data volume (created at runtime)
```

### Key Files

- **Dockerfile** - Builds the Spring Boot application image (multi-stage build)
- **docker-compose.yml** - Orchestrates MySQL, Application, and Nginx services
- **nginx.conf** - Reverse proxy configuration (routes traffic to app)

### Running from docker/ folder

```bash
cd docker
docker-compose up --build        # Start all services
docker-compose down              # Stop all services
docker-compose ps                # Show running services
docker-compose logs -f           # Stream all logs
docker-compose logs -f mysql     # Stream MySQL logs only
docker-compose logs -f clinic-app # Stream app logs only
```

### Running from root folder

```bash
docker-compose -f docker/docker-compose.yml up --build
docker-compose -f docker/docker-compose.yml down
```

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

- **API Endpoint**: `http://localhost:8080/api/`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Health Check**: `http://localhost:8080/actuator/health`

## MySQL Management

### Connect to MySQL Container

```bash
docker exec -it clinic-mysql mysql -u clinic_user -p clinic_db
```

Password: `clinic_password`

### View MySQL Logs

```bash
docker-compose logs -f mysql
```

### Backup MySQL Data

```bash
docker exec clinic-mysql mysqldump -u clinic_user -p clinic_db > backup.sql
```

## Troubleshooting

### Application fails to start

1. Check MySQL is healthy:
   ```bash
   docker-compose logs mysql
   ```

2. Verify connection string in `application-docker.properties`

3. Ensure MySQL container has startup delay (health checks configured)

### MySQL connection refused

1. Verify MySQL service is running:
   ```bash
   docker-compose ps
   ```

2. Check MySQL logs:
   ```bash
   docker-compose logs mysql
   ```

3. Verify network connectivity:
   ```bash
   docker network ls
   ```

### Data not persisting

1. Check mysql-data volume exists:
   ```bash
   docker volume ls | grep mysql-data
   ```

2. Verify volume mapping in docker-compose.yml

3. Ensure volume has write permissions

### Performance issues

1. Monitor container resources:
   ```bash
   docker stats
   ```

2. Check application logs:
   ```bash
   docker-compose logs -f clinic-app
   ```

3. Review MySQL slow query logs

## Docker Compose Commands

### Quick Start (Recommended)

**Start all services with one command:**
```bash
cd docker
docker-compose up --build
```

This will:
1. Build the Docker image
2. Create the Docker network and volume
3. Start MySQL 8.0 container
4. Wait for MySQL to be healthy
5. Start the application container
6. Start Nginx reverse proxy

**Stop all services:**
```bash
docker-compose down
```

**Stop and remove all data (clean slate):**
```bash
docker-compose down -v
```

### Service Management

**Start services in background (detached mode):**
```bash
docker-compose up --build -d
```

**Start services without rebuilding:**
```bash
docker-compose up
```

**Stop all services (keep data):**
```bash
docker-compose stop
```

**Start already-stopped services:**
```bash
docker-compose start
```

**Restart all services:**
```bash
docker-compose restart
```

### Monitoring & Logs

**View status of all containers:**
```bash
docker-compose ps
```

**Stream logs from all services:**
```bash
docker-compose logs -f
```

**Stream logs from specific service:**
```bash
# Application logs
docker-compose logs -f clinic-app

# MySQL logs
docker-compose logs -f mysql

# Nginx logs
docker-compose logs -f nginx
```

**View last 200 lines of logs:**
```bash
docker-compose logs --tail 200
```

**Monitor container resource usage:**
```bash
docker stats
```

### Building

**Rebuild application image:**
```bash
docker-compose build --no-cache
```

**Rebuild specific service:**
```bash
docker-compose build --no-cache clinic-app
```

### Database Operations

**Connect to MySQL container:**
```bash
docker exec -it clinic-mysql mysql -u clinic_user -p clinic_db
```
Password: `clinic_password`

**Execute SQL query directly:**
```bash
docker exec clinic-mysql mysql -u clinic_user -pclinic_password clinic_db -e "SELECT * FROM patients;"
```

**Backup MySQL database:**
```bash
docker exec clinic-mysql mysqldump -u clinic_user -pclinic_password clinic_db > backup.sql
```

**Restore MySQL database:**
```bash
docker exec -i clinic-mysql mysql -u clinic_user -pclinic_password clinic_db < backup.sql
```

### Debugging

**View container logs with timestamps:**
```bash
docker-compose logs --timestamps
```

**Execute command in running container:**
```bash
docker exec -it clinic-appointment-system /bin/sh
```

**Check network connectivity:**
```bash
docker exec clinic-appointment-system ping clinic-mysql
```

**Inspect Docker network:**
```bash
docker network inspect docker_clinic-network
```

**List Docker volumes:**
```bash
docker volume ls
```

### Complete Workflow

**Development workflow:**
```bash
# Navigate to docker folder
cd docker

# Start fresh
docker-compose down -v
docker-compose up --build

# In another terminal, watch logs
docker-compose logs -f clinic-app

# When done, stop services
docker-compose down
```

**Production-like workflow:**
```bash
# Start in detached mode
cd docker
docker-compose up --build -d

# Verify services are healthy
docker-compose ps

# Check application is responding
curl http://localhost:8080/actuator/health

# View logs if needed
docker-compose logs clinic-app

# Stop services
docker-compose down
```

## Deployment Checklist

- [ ] Java 17+ installed (for local deployment)
- [ ] Docker and Docker Compose installed
- [ ] MySQL 8.0 configured (or use docker-compose)
- [ ] Application built successfully
- [ ] All tests passing
- [ ] Environment variables configured
- [ ] Health checks passing
- [ ] API endpoints accessible
- [ ] Data persistence verified
- [ ] Backups configured