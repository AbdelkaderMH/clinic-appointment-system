# Kubernetes Deployment Guide ☸️

This document explains the Kubernetes manifests for deploying the Clinic Appointment System to a Kubernetes cluster (Minikube).

## Files Overview

The `k8s/` directory contains 8 Kubernetes manifest files for complete application deployment:

```
k8s/
├── namespace.yaml           # Namespace isolation
├── secret.yaml             # Database credentials
├── mysql.yaml              # MySQL database deployment & service
├── deployment.yaml         # Application deployment
├── service.yaml            # Application service (NodePort)
├── ingress.yaml            # Ingress controller routing
├── servicemonitor.yaml     # Prometheus monitoring
└── servicemonitor-clinic.yaml # Additional monitoring
```

## Deployment Order

The Jenkins pipeline applies manifests in this order:

1. **Namespace** → **Secret** → **MySQL** → **Application** → **Service**

## File Explanations

### 1. namespace.yaml
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: clinic
```

**Purpose**: Creates isolated namespace for all clinic resources
**Role**: 
- Provides resource isolation and organization
- Enables namespace-scoped RBAC and network policies
- Used by all other manifests with `namespace: clinic`

### 2. secret.yaml
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: mysql-secret
  namespace: clinic
type: Opaque
stringData:
  MYSQL_ROOT_PASSWORD: root
  MYSQL_DATABASE: clinic_db
  MYSQL_USER: clinic_user
  MYSQL_PASSWORD: clinic_password
```

**Purpose**: Stores sensitive database configuration
**Role**:
- Securely stores MySQL credentials
- Referenced by both MySQL and application deployments
- Uses `stringData` for plain text input (automatically base64 encoded)

### 3. mysql.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
# MySQL deployment with service
```

**Purpose**: Deploys MySQL 8.0 database with persistent storage
**Components**:
- **Deployment**: MySQL container with resource limits and health checks
- **Service**: ClusterIP service for internal database access

**Key Features**:
- Resource limits: 512Mi-1Gi memory, 250m-500m CPU
- Liveness/readiness probes using `mysqladmin ping`
- Environment variables from `mysql-secret`
- EmptyDir volume for data storage (non-persistent in this setup)

### 4. deployment.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: clinic-appointment-system
```

**Purpose**: Deploys the Spring Boot application
**Key Features**:
- **Image**: `mhabdelkader/clinic-app:latest` (updated by Jenkins with build number)
- **Security**: Non-root user (1000), dropped capabilities, no privilege escalation
- **Environment**: Docker profile, MySQL connection via service name
- **Health Checks**: 
  - Liveness: `/actuator/health` (60s delay, 30s interval)
  - Readiness: `/actuator/health/readiness` (30s delay, 10s interval)
- **Resources**: 512Mi-1Gi memory, 250m-500m CPU

### 5. service.yaml
```yaml
apiVersion: v1
kind: Service
metadata:
  name: clinic-appointment-system
spec:
  type: NodePort
  ports:
    - port: 8090
      targetPort: 8090
      nodePort: 30090
```

**Purpose**: Exposes application externally via NodePort
**Role**:
- **NodePort 30090**: External access via Minikube IP
- **Port 8090**: Internal cluster communication
- **Prometheus Annotations**: Enables metrics scraping
- **Session Affinity**: None (stateless application)

### 6. ingress.yaml
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: clinic-appointment-ingress
spec:
  rules:
    - host: clinic.local
```

**Purpose**: Provides HTTP routing via domain name
**Role**:
- Routes `clinic.local` traffic to the application service
- Requires NGINX Ingress Controller
- Disables SSL redirect for local development
- Path-based routing with prefix matching

### 7. servicemonitor.yaml
```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: clinic-appointment-system
spec:
  endpoints:
  - port: http
    path: /actuator/prometheus
```

**Purpose**: Configures Prometheus monitoring
**Role**:
- Defines scraping configuration for Prometheus
- Targets `/actuator/prometheus` endpoint
- 30-second scrape interval with 10-second timeout
- Requires Prometheus Operator

## Deployment Commands

### Manual Deployment
```bash
# Apply in order
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/mysql.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/servicemonitor.yaml
```

### Jenkins Pipeline Deployment
The Jenkins pipeline automates deployment:
```bash
kubectl apply -n clinic -f k8s/secret.yaml
kubectl apply -n clinic -f k8s/mysql.yaml
kubectl rollout status deployment/mysql -n clinic --timeout=180s
kubectl apply -n clinic -f k8s/deployment.yaml
kubectl apply -n clinic -f k8s/service.yaml
kubectl set image deployment/clinic-appointment-system clinic-appointment-system=mhabdelkader/clinic-app:${BUILD_NUMBER} -n clinic
kubectl rollout status deployment/clinic-appointment-system -n clinic --timeout=180s
```

## Access Methods

### 1. NodePort Access
```bash
# Get Minikube IP
minikube ip

# Access application
curl http://<MINIKUBE_IP>:30090/actuator/health
```

### 2. Port Forwarding
```bash
kubectl port-forward -n clinic svc/clinic-appointment-system 8090:8090
# Access: http://localhost:8090
```

### 3. Ingress Access
```bash
# Add to /etc/hosts (Linux/Mac) or C:\Windows\System32\drivers\etc\hosts (Windows)
<MINIKUBE_IP> clinic.local

# Access: http://clinic.local
```

## Resource Requirements

### MySQL
- **Memory**: 512Mi request, 1Gi limit
- **CPU**: 250m request, 500m limit
- **Storage**: EmptyDir (ephemeral)

### Application
- **Memory**: 512Mi request, 1Gi limit  
- **CPU**: 250m request, 500m limit
- **Replicas**: 1 (can be scaled)

## Monitoring Integration

### Prometheus Metrics
- **Service Annotations**: Enable automatic discovery
- **ServiceMonitor**: Custom resource for Prometheus Operator
- **Endpoint**: `/actuator/prometheus`
- **Scrape Interval**: 30 seconds

### Health Checks
- **Liveness**: `/actuator/health` - restarts pod if unhealthy
- **Readiness**: `/actuator/health/readiness` - removes from service if not ready

## Security Features

### Application Security
- **Non-root user**: UID 1000
- **Dropped capabilities**: ALL capabilities removed
- **No privilege escalation**: `allowPrivilegeEscalation: false`
- **Security context**: Pod and container level security

### Secret Management
- **Kubernetes Secrets**: Base64 encoded credential storage
- **Environment injection**: Secrets mounted as environment variables
- **Namespace isolation**: Secrets scoped to clinic namespace

## Troubleshooting

### Common Commands
```bash
# Check pod status
kubectl get pods -n clinic

# View application logs
kubectl logs -n clinic deployment/clinic-appointment-system

# Check service endpoints
kubectl get endpoints -n clinic

# Describe problematic resources
kubectl describe pod <pod-name> -n clinic

# Check resource usage
kubectl top pods -n clinic
```

### Common Issues
1. **ImagePullBackOff**: Verify Docker image exists and is accessible
2. **CrashLoopBackOff**: Check application logs and health check endpoints
3. **Service connection issues**: Verify service selectors match pod labels
4. **Database connection**: Ensure MySQL pod is ready before application starts

## Integration with CI/CD

The Kubernetes manifests integrate with the Jenkins pipeline:

1. **Build**: Creates Docker image with build number tag
2. **Deploy**: Applies manifests to Minikube cluster  
3. **Update**: Sets new image tag on deployment
4. **Verify**: Waits for rollout completion and health checks
5. **Monitor**: PowerShell script validates deployment success

See `PIPELINE.md` for complete CI/CD workflow details.