# Clinic Appointment Scheduling System 🏥

A comprehensive clinic appointment scheduling application built with
**Spring Boot 3.2.0**, **Java 21**, **MySQL 8.0**, and complete testing support.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/downloads/#java21)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)](https://dev.mysql.com/downloads/mysql/)
[![Jenkins](https://img.shields.io/badge/Jenkins-Pipeline-blue.svg)](Jenkinsfile)
[![SonarQube](https://img.shields.io/badge/SonarQube-Quality-4E9BCD.svg)](https://www.sonarqube.org/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED.svg)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Deployed-326CE5.svg)](https://kubernetes.io/)
[![Prometheus](https://img.shields.io/badge/Prometheus-Monitoring-E6522C.svg)](https://prometheus.io/)
[![Grafana](https://img.shields.io/badge/Grafana-Dashboards-F46800.svg)](https://grafana.com/)

## 🚀 Features

* **Patient management** – complete CRUD operations for patient records.
* **Doctor management** – manage doctor profiles and specializations.
* **Appointment scheduling** – book, cancel, and manage appointments with
  conflict prevention to avoid double booking.
* **RESTful API** – fully documented endpoints with proper HTTP status codes.
* **Comprehensive testing** – unit and integration tests with JUnit 5,
  Mockito and Spring Boot Test.
* **API documentation** – auto‑generated OpenAPI/Swagger specification.
* **Docker support** – containerized deployment ready for production.
* **CI/CD** – Jenkins pipeline with automated testing and deployment.
* **Monitoring** – Prometheus metrics and health checks.

## 🛠️ Technology Stack

### Backend Framework
- **Spring Boot 3.2.0** - Main application framework
- **Spring Data JPA** - Database abstraction layer
- **Spring Web** - RESTful web services
- **Spring Validation** - Input validation
- **Spring Actuator** - Application monitoring and health checks

### Database
- **MySQL 8.0** - Primary database
- **H2** - In-memory database for testing

### Documentation & API
- **SpringDoc OpenAPI 3** - API documentation and Swagger UI
- **Jackson** - JSON serialization/deserialization

### Testing
- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing
- **JaCoCo** - Code coverage analysis

### DevOps & Monitoring
- **Docker** - Containerization
- **Kubernetes** - Container orchestration
- **Jenkins** - CI/CD pipeline
- **SonarQube** - Code quality analysis
- **Prometheus** - Metrics collection
- **Micrometer** - Application metrics

### Jenkins Plugins
- **SonarQube Scanner for Jenkins** - Code quality analysis
- **Maven Integration plugin** - Maven build support
- **Docker plugin** - Docker build and push
- **Kubernetes plugin** - kubectl commands

## 🏗️ Architecture

This application follows a typical layered architecture with MySQL as the persistence layer:

```
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   Controller   │───▶│    Service    │───▶│   Repository   │
│   (REST API)   │    │   (Business)  │    │   (Database)   │
└───────────────┘    └───────────────┘    └───────────────┘
      │                     │                     │
      ▼                     ▼                     ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│      DTO       │    │     Model     │    │   MySQL 8.0    │
│ (Data Transfer)│    │   (Entities)  │    │  (Persistence) │
└───────────────┘    └───────────────┘    └───────────────┘
```

## 📋 Prerequisites

* **Java 21** or higher
* **Maven 3.6** or higher
* **MySQL 8.0** (for local development)
* **Docker** (optional, for containerized deployment)
* **Kubernetes/Minikube** (for K8s deployment)

## 🚀 Quick Start

### Option 1: Docker (Recommended)

```bash
cd clinic-appointment-system/docker
docker-compose up --build
```

The service will start on `http://localhost:8090`. Swagger UI is available at
`http://localhost:8090/swagger-ui.html`. MySQL runs in a separate container with persistent storage.

### Option 2: Local Development

Clone the repository and build the project:

```bash
git clone https://github.com/AbdelkaderMH/clinic-appointment-system.git
cd clinic-appointment-system
mvn clean install
mvn spring-boot:run
```

Ensure MySQL 8.0 is running locally on port 3306 with the default credentials configured in `application.properties`.

The service will start on `http://localhost:8090`. Swagger UI is available at
`http://localhost:8090/swagger-ui.html`. Use the API endpoints to manage
patients, doctors and appointments.

### Option 3: Full CI/CD Pipeline with Monitoring

Run the complete Jenkins CI/CD pipeline that includes automated deployment and monitoring using **Minikube** as the Kubernetes cluster manager:

#### Minikube Cluster Management:
```bash
# Start Minikube cluster
minikube start

# Check cluster status
minikube status
```

#### Jenkins Pipeline Steps:
1. **Environment Validation** - Validates required environment variables
2. **Checkout** - Pulls source code from Git repository
3. **Build & Unit Tests** - Maven build with JUnit tests and JaCoCo coverage
4. **SonarQube Analysis** - Code quality analysis (localhost:9000)
5. **Build Docker Image** - Creates containerized application
6. **Push Docker Image** - Pushes to DockerHub registry
7. **Deploy to Kubernetes** - Deploys to Minikube cluster
8. **PowerShell Monitoring** - Final validation and monitoring setup

#### PowerShell Monitoring Script:
The pipeline concludes with the monitoring script that:
- Sets up Prometheus and Grafana monitoring stack
- Validates Kubernetes cluster health
- Configures port forwarding for access
- Verifies application deployment success

```bash
# Trigger Jenkins pipeline or run monitoring script directly
powershell .\minikubemonitoring.ps1
```

#### Access Points After Pipeline:
- **Application**: `http://localhost:8090`
- **Swagger UI**: `http://localhost:8090/swagger-ui/index.html`
- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3000` (admin/admin)
  - Import Dashboard ID **4701** for Spring Boot monitoring
  - Import Dashboard ID **6417** for Kubernetes cluster monitoring
- **Jenkins**: Your Jenkins instance for pipeline monitoring
- **SonarQube**: `http://localhost:9000` for code quality reports

#### Pipeline Success Criteria:
✅ All unit tests pass with >80% coverage  
✅ SonarQube quality gate passed  
✅ Docker image built and pushed successfully  
✅ Kubernetes deployment completed  
✅ PowerShell monitoring validates system health

## 📊 Monitoring

The application includes comprehensive monitoring capabilities:

- **Prometheus Metrics**: `http://localhost:9090`
- **Grafana Dashboards**: `http://localhost:3000` (admin/admin)
- **Application Health**: `http://localhost:8090/actuator/health`
- **Application Metrics**: `http://localhost:8090/actuator/prometheus`
- **Swagger API Documentation**: `http://localhost:8090/swagger-ui/index.html`

### Grafana Dashboard Setup

**Quick Import (Recommended):**
1. Access Grafana: `http://localhost:3000` (admin/admin)
2. Click **+** → **Import**
3. Use Dashboard ID: **4701** (Spring Boot 2.1 System Monitor)
4. Select **Prometheus** as data source
5. Click **Import**

**Alternative Dashboard IDs:**
- **6417** - Kubernetes Cluster Monitoring
- **11378** - JVM Micrometer
- **12900** - Spring Boot Statistics
- **10280** - Spring Boot Observability
- **7362** - MySQL Overview


**Custom Dashboard:**
Use the included `grafana-dashboard.json` template for clinic-specific metrics.

The PowerShell monitoring script automatically sets up the complete monitoring stack with port forwarding.

## ⚖️ Scaling

### Scale Application Replicas

To duplicate your clinic app containers in Kubernetes:

```bash
# Scale to 3 replicas using kubectl
kubectl scale deployment clinic-appointment-system --replicas=3 -n clinic

# Or update deployment.yaml and change replicas: 1 to replicas: 3
kubectl apply -f k8s/deployment.yaml

# Verify scaling
kubectl get pods -n clinic
kubectl get deployment clinic-appointment-system -n clinic
```

The Kubernetes service automatically load balances traffic between all replicas.

## 📚 Documentation

- **[PIPELINE.md](./PIPELINE.md)** - Jenkins CI/CD pipeline guide
- **[docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md)** - System architecture and design
- **[docs/DEPLOYMENT.md](./docs/DEPLOYMENT.md)** - Kubernetes deployment guide
- **[docs/SETUP.md](./docs/SETUP.md)** - Development environment setup
- **[docs/TESTING.md](./docs/TESTING.md)** - Testing strategies and coverage
- **[docs/API.md](./docs/API.md)** - REST API documentation
- **[docker/README.md](./docker/README.md)** - Docker configuration guide
- **[k8s/README.md](./k8s/README.md)** - Kubernetes deployment guide

## 📁 Project Structure

The project is organised as follows:

```
clinic-appointment-system/
├── docs/                 # Project documentation
│   ├── API.md           # API reference
│   ├── ARCHITECTURE.md  # System architecture
│   ├── DEPLOYMENT.md    # Deployment guide
│   ├── SETUP.md         # Environment setup
│   └── TESTING.md       # Testing guide
├── docker/              # Docker configuration
│   ├── Dockerfile       # Application container
│   ├── docker-compose.yml # Multi-container setup
│   ├── nginx.conf       # Nginx configuration
│   └── README.md        # Docker usage guide
├── k8s/                 # Kubernetes manifests
│   ├── deployment.yaml  # App deployment
│   ├── service.yaml     # Service definition
│   ├── mysql.yaml       # Database deployment
│   ├── namespace.yaml   # Namespace configuration
│   ├── secret.yaml      # Database credentials
│   ├── ingress.yaml     # Ingress controller
│   ├── servicemonitor.yaml # Prometheus monitoring
│   └── README.md        # Kubernetes deployment guide
├── scripts/             # Utility scripts
│   ├── build.sh         # Build script
│   ├── run.sh           # Run script
│   └── test.sh          # Test script
├── src/                 # Application source code
│   ├── main/
│   │   ├── java/com/clinic/     # Java source files
│   │   └── resources/          # Configuration files
│   └── test/
│       ├── java/               # Test source files
│       └── resources/          # Test resources
├── Jenkinsfile          # Jenkins CI/CD pipeline
├── PIPELINE.md          # Pipeline documentation
├── minikubemonitoring.ps1 # Monitoring script
├── pom.xml              # Maven build file
└── README.md            # This file
```

See `docs/ARCHITECTURE.md` for detailed system design and package structure.

## 📝 License

This project is licensed under the MIT License. See the `LICENSE` file for
details.