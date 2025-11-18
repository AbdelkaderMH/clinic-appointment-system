# Docker Configuration Guide 🐳

This document explains the Docker setup for the Clinic Appointment System, including the Dockerfile and Docker Compose configuration.

## Files Overview

- **`docker/Dockerfile`** - Multi-stage build for the Spring Boot application
- **`docker/docker-compose.yml`** - Complete application stack with MySQL database

## Dockerfile Explanation

### Multi-Stage Build

```dockerfile
FROM maven:3.9.9-eclipse-temurin-21 AS build
```

**Stage 1: Build Stage**
- Uses Maven with Java 21 for building the application
- Leverages Docker layer caching by copying `pom.xml` first
- Downloads dependencies before copying source code
- Builds the JAR file with `mvn clean package -DskipTests`

```dockerfile
FROM eclipse-temurin:21-jre-alpine
```

**Stage 2: Runtime Stage**
- Uses lightweight Alpine Linux with Java 21 JRE
- Installs `curl` for health checks
- Creates non-root user `appuser` for security
- Copies built JAR and data files
- Exposes port **8090**

### Security Features

- **Non-root user**: Application runs as `appuser` (UID 1001)
- **Minimal base image**: Alpine Linux reduces attack surface
- **Health checks**: Built-in health monitoring

## Docker Compose Configuration

### Services

#### MySQL Database
```yaml
mysql:
  image: mysql:8.0
  environment:
    MYSQL_ROOT_PASSWORD: root
    MYSQL_DATABASE: clinic_db
    MYSQL_USER: clinic_user
    MYSQL_PASSWORD: clinic_password
  ports:
    - "3306:3306"
```

**Features:**
- MySQL 8.0 with persistent storage
- Health checks with `mysqladmin ping`
- Initialization scripts support
- Dedicated network isolation

#### Clinic Application
```yaml
clinic-app:
  build:
    context: ..
    dockerfile: docker/Dockerfile
  environment:
    SPRING_PROFILES_ACTIVE: docker
    SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/clinic_db
  ports:
    - "8090:8090"
```

**Features:**
- Builds from local Dockerfile
- Uses Docker-specific Spring profile
- Connects to MySQL container via service name
- Health checks on `/actuator/health`
- Depends on MySQL being healthy

### Networking

```yaml
networks:
  clinic-network:
    driver: bridge
```

- Isolated bridge network for service communication
- Services communicate using container names as hostnames

### Volumes

```yaml
volumes:
  mysql_data:
    driver: local
```

- Persistent storage for MySQL data
- Survives container restarts and recreations

## Usage

### Quick Start

```bash
cd clinic-appointment-system/docker
docker-compose up --build
```

### Development Mode

```bash
# Build only
docker-compose build

# Run in background
docker-compose up -d

# View logs
docker-compose logs -f clinic-app

# Stop services
docker-compose down
```

### Production Deployment

```bash
# Build and run with restart policy
docker-compose up -d --build
```

## Access Points

After running `docker-compose up`:

- **Application**: `http://localhost:8090`
- **Swagger UI**: `http://localhost:8090/swagger-ui/index.html`
- **Health Check**: `http://localhost:8090/actuator/health`
- **MySQL**: `localhost:3306` (external access)

## Environment Variables

### Application Container
- `SPRING_PROFILES_ACTIVE=docker`
- `SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/clinic_db`
- `SPRING_DATASOURCE_USERNAME=clinic_user`
- `SPRING_DATASOURCE_PASSWORD=clinic_password`

### MySQL Container
- `MYSQL_ROOT_PASSWORD=root`
- `MYSQL_DATABASE=clinic_db`
- `MYSQL_USER=clinic_user`
- `MYSQL_PASSWORD=clinic_password`

## Health Checks

Both services include health checks:

### MySQL
```yaml
healthcheck:
  test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
  interval: 10s
  timeout: 5s
  retries: 5
```

### Application
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8090/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
```

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 3306 and 8090 are available
2. **Build failures**: Check Java 21 and Maven compatibility
3. **Database connection**: Verify MySQL container is healthy before app starts
4. **Health check failures**: Wait for application startup (can take 60+ seconds)

### Useful Commands

```bash
# Check container status
docker-compose ps

# View application logs
docker-compose logs clinic-app

# Access MySQL container
docker-compose exec mysql mysql -u clinic_user -p clinic_db

# Rebuild specific service
docker-compose build clinic-app

# Remove all containers and volumes
docker-compose down -v
```

## Integration with CI/CD

The Docker configuration integrates with the Jenkins pipeline:

1. **Build Stage**: Creates Docker image with build number tag
2. **Push Stage**: Pushes to DockerHub registry
3. **Deploy Stage**: Updates Kubernetes deployment with new image

See `PIPELINE.md` for complete CI/CD workflow details.