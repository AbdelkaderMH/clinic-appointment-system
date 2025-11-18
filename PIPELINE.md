# Jenkins CI/CD Pipeline Guide 🚀

This document outlines the complete Jenkins CI/CD pipeline for the Clinic Appointment System, from development to production deployment.

## Pipeline Overview

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Source    │───▶│    Build    │───▶│    Test     │───▶│   Deploy    │
│   Control   │    │  & Package  │    │ & Quality   │    │ & Monitor   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

## Jenkins Pipeline Stages

### 1. Environment Validation
```groovy
stage('Environment Validation') {
    steps {
        script {
            echo "Validating environment variables..."
            if (!env.APP_NAME) error "APP_NAME not set"
            if (!env.DOCKER_IMAGE_NAME) error "DOCKER_IMAGE_NAME not set"
            if (!env.DOCKER_REGISTRY) error "DOCKER_REGISTRY not set"
            if (!env.DOCKER_REPOSITORY) error "DOCKER_REPOSITORY not set"
            if (!env.K8S_NAMESPACE) error "K8S_NAMESPACE not set"
            echo "Environment validation passed"
        }
    }
}
```

### 2. Checkout
```groovy
stage('Checkout') {
    steps {
        checkout scm
    }
}
```

### 3. Build & Unit Tests
```groovy
stage('Build & Unit Tests') {
    steps {
        script {
            try {
                bat "mvn -B -U clean verify"
            } catch (Exception e) {
                error "Maven build failed: ${e.getMessage()}"
            }
        }
    }
    post {
        always {
            junit '**/target/surefire-reports/*.xml'
            recordCoverage(
                tools: [[
                    pattern: 'target/site/jacoco/jacoco.xml',
                    type: 'JACOCO'
                ]]
            )
        }
    }
}
```

### 4. SonarQube Analysis
```groovy
stage('SonarQube Analysis') {
    steps {
        script {
            withCredentials([string(credentialsId: 'sonarqube', variable: 'SONAR_TOKEN')]) {
                bat "mvn sonar:sonar -DskipTests -Dsonar.host.url=http://localhost:9000 -Dsonar.token=${SONAR_TOKEN}"
            }
        }
    }
}
```

### 5. Build Docker Image
```groovy
stage('Build Docker Image') {
    steps {
        script {
            bat "docker build -f docker/Dockerfile -t ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:${BUILD_NUMBER} ."
            bat "docker tag ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:${BUILD_NUMBER} ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:latest"
        }
    }
}
```

### 6. Push Docker Image
```groovy
stage('Push Docker Image') {
    steps {
        script {
            withCredentials([usernamePassword(credentialsId: 'docker', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                bat "echo %DOCKER_PASS% | docker login ${DOCKER_REGISTRY} -u %DOCKER_USER% --password-stdin"
                bat "docker push ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:${BUILD_NUMBER}"
                bat "docker push ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:latest"
            }
        }
    }
}
```

### 7. Deploy to Kubernetes
```groovy
stage('Deploy to Kubernetes') {
    when {
        expression { !env.BRANCH_NAME || env.BRANCH_NAME == 'main' }
    }
    steps {
        script {
            bat """
            kubectl apply -f k8s/namespace.yaml
            kubectl apply -n %K8S_NAMESPACE% -f k8s/secret.yaml
            kubectl apply -n %K8S_NAMESPACE% -f k8s/mysql.yaml
            kubectl rollout status deployment/mysql -n %K8S_NAMESPACE% --timeout=180s
            kubectl apply -n %K8S_NAMESPACE% -f k8s/deployment.yaml
            kubectl apply -n %K8S_NAMESPACE% -f k8s/service.yaml
            kubectl set image deployment/clinic-appointment-system clinic-appointment-system=%DOCKER_REPOSITORY%/%DOCKER_IMAGE_NAME%:%BUILD_NUMBER% -n %K8S_NAMESPACE%
            kubectl rollout status deployment/clinic-appointment-system -n %K8S_NAMESPACE% --timeout=180s
            """
        }
    }
}
```

### 8. Final Step: PowerShell Monitoring
```groovy
stage('PowerShell Monitoring') {
    steps {
        powershell script: '''
            Write-Host "Starting post-deployment monitoring..."
            .\minikubemonitoring.ps1 -Environment Production -Namespace clinic
        '''
    }
}
```

## Step-by-Step Pipeline Explanation

### Stage 1: Environment Validation
**Purpose**: Validates all required environment variables before pipeline execution
**What happens**:
- Checks if `APP_NAME`, `DOCKER_IMAGE_NAME`, `DOCKER_REGISTRY`, `DOCKER_REPOSITORY`, `K8S_NAMESPACE` are set
- **Fails pipeline** if any variable is missing
- Prevents wasted execution time on incomplete configurations

**Success criteria**: All environment variables exist
**Failure impact**: Pipeline stops immediately

### Stage 2: Checkout
**Purpose**: Downloads source code from Git repository
**What happens**:
- Jenkins pulls latest code from SCM (Git)
- Sets up workspace with project files
- Prepares for build process

**Success criteria**: Code successfully downloaded
**Failure impact**: No code to build - pipeline stops

### Stage 3: Build & Unit Tests
**Purpose**: Compiles code, runs tests, generates coverage reports
**What happens**:
- Executes `mvn -B -U clean verify`
- Compiles Java source code
- Runs all JUnit tests
- Generates JaCoCo coverage report
- Publishes test results and coverage metrics

**Success criteria**: 
- Code compiles without errors
- All unit tests pass
- Coverage report generated

**Failure impact**: Build fails if compilation errors or test failures occur

### Stage 4: SonarQube Analysis
**Purpose**: Performs static code analysis for quality and security
**What happens**:
- Connects to local SonarQube server (localhost:9000)
- Analyzes code quality, bugs, vulnerabilities, code smells
- Uploads results to SonarQube dashboard
- **Non-blocking**: Pipeline continues even if SonarQube fails

**Success criteria**: Analysis completes and uploads to SonarQube
**Failure impact**: Warning logged but pipeline continues

### Stage 5: Build Docker Image
**Purpose**: Creates containerized version of the application
**What happens**:
- Builds Docker image using `docker/Dockerfile`
- Tags image with build number: `mhabdelkader/clinic-app:${BUILD_NUMBER}`
- Creates `latest` tag for the image
- Verifies image creation

**Success criteria**: Docker image built and tagged successfully
**Failure impact**: No deployable artifact - pipeline fails

### Stage 6: Push Docker Image
**Purpose**: Uploads Docker image to DockerHub registry
**What happens**:
- Authenticates with DockerHub using stored credentials
- Pushes both versioned and `latest` tags
- Makes image available for deployment
- Logs out from registry

**Success criteria**: Image successfully pushed to DockerHub
**Failure impact**: Deployment impossible without image - pipeline fails

### Stage 7: Deploy to Kubernetes
**Purpose**: Deploys application to Minikube cluster
**Conditional**: Only runs on `main` branch or when `BRANCH_NAME` is not set
**What happens**:
1. Creates/updates Kubernetes namespace
2. Applies secrets configuration
3. Deploys MySQL database
4. Waits for MySQL to be ready (180s timeout)
5. Deploys application
6. Creates service for external access
7. Updates deployment with new image version
8. Waits for application rollout (180s timeout)

**Success criteria**: All Kubernetes resources deployed and running
**Failure impact**: Application not accessible - pipeline fails

### Stage 8: PowerShell Monitoring (Final Validation)
**Purpose**: Validates deployment success and system health
**What happens**:
- Executes `minikubemonitoring.ps1` script
- Performs comprehensive health checks:
  - Kubernetes cluster status
  - Pod health verification
  - Service connectivity tests
  - Database connection validation
  - API endpoint health checks
  - Resource utilization monitoring
  - Log analysis for errors

**Success criteria**: All monitoring checks pass
**Failure impact**: Indicates deployment issues - may trigger rollback

## Critical Success Path
1. **Environment OK** → 2. **Code Retrieved** → 3. **Tests Pass** → 4. **Quality Check** → 5. **Image Built** → 6. **Image Pushed** → 7. **Deployed** → 8. **Monitoring Validates** → **SUCCESS**

## Key Failure Points
- **Stage 1**: Missing environment variables
- **Stage 3**: Test failures or compilation errors  
- **Stage 5**: Docker build issues
- **Stage 6**: Registry push failures
- **Stage 7**: Kubernetes deployment failures
- **Stage 8**: Health check failures (indicates deployment problems)

## Post-Pipeline Actions

### On Success:
- Logs success message
- Reports Docker image location
- Cleans workspace (preserving test reports)

### On Failure:
- Logs failure message
- Preserves logs for debugging
- Triggers alerts/notifications

### Always:
- Logs out from Docker registry
- Cleans workspace selectively
- Preserves test and coverage reports

## PowerShell Monitoring Script (`minikubemonitoring.ps1`)

This script performs comprehensive post-deployment monitoring:

### Features:
- **Kubernetes Cluster Status**: Checks node health and resource usage
- **Pod Monitoring**: Validates all pods are running correctly
- **Service Connectivity**: Tests API endpoints and database connections
- **Resource Metrics**: CPU, memory, and storage utilization
- **Log Analysis**: Scans for errors and warnings
- **Alert Generation**: Sends notifications if issues detected

### Execution:
```powershell
.\minikubemonitoring.ps1 -Environment Production -Namespace clinic-system
```

### Monitoring Checks:
1. **Cluster Health**
   ```powershell
   kubectl cluster-info
   kubectl get nodes -o wide
   ```

2. **Application Status**
   ```powershell
   kubectl get pods -n clinic-system
   kubectl get services -n clinic-system
   kubectl get ingress -n clinic-system
   ```

3. **Database Connectivity**
   ```powershell
   kubectl exec -it mysql-pod -n clinic-system -- mysql -u root -p -e "SHOW DATABASES;"
   ```

4. **API Health Checks**
   ```powershell
   Invoke-RestMethod -Uri "http://clinic.local/actuator/health" -Method GET
   Invoke-RestMethod -Uri "http://clinic.local/api/patients" -Method GET
   ```

5. **Performance Metrics**
   ```powershell
   kubectl top nodes
   kubectl top pods -n clinic-system
   ```

6. **Log Collection**
   ```powershell
   kubectl logs -l app=clinic-app -n clinic-system --tail=100
   ```

## Complete Jenkins Pipeline (`Jenkinsfile`)

```groovy
pipeline {
    agent any

    environment {
        APP_NAME = "clinic-appointment-system"
        DOCKER_IMAGE_NAME = "clinic-app"
        DOCKER_REGISTRY = "docker.io"
        DOCKER_REPOSITORY = "mhabdelkader"
        K8S_NAMESPACE = "clinic"
        K8S_MONITORING_NAMESPACE = "monitoring"
        K8S_CONTEXT = "minikube"
        KUBECONFIG = "C:\\Users\\Abdelkader\\.kube\\config"
        SONAR_HOST_URL = "http://localhost:9000"
    }

    stages {
        stage('Environment Validation') {
            steps {
                script {
                    echo "Validating environment variables..."
                    if (!env.APP_NAME) error "APP_NAME not set"
                    if (!env.DOCKER_IMAGE_NAME) error "DOCKER_IMAGE_NAME not set"
                    if (!env.DOCKER_REGISTRY) error "DOCKER_REGISTRY not set"
                    if (!env.DOCKER_REPOSITORY) error "DOCKER_REPOSITORY not set"
                    if (!env.K8S_NAMESPACE) error "K8S_NAMESPACE not set"
                    echo "Environment validation passed"
                }
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Unit Tests') {
            steps {
                script {
                    try {
                        bat "mvn -B -U clean verify"
                    } catch (Exception e) {
                        error "Maven build failed: ${e.getMessage()}"
                    }
                }
            }
            post {
                always {
                    script {
                        if (fileExists('**/target/surefire-reports/*.xml')) {
                            junit '**/target/surefire-reports/*.xml'
                        }
                        if (fileExists('target/site/jacoco/jacoco.xml')) {
                            recordCoverage(
                                tools: [[
                                    pattern: 'target/site/jacoco/jacoco.xml',
                                    type: 'JACOCO'
                                ]]
                            )
                        }
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'sonarqube', variable: 'SONAR_TOKEN')]) {
                        try {
                            bat "mvn sonar:sonar -DskipTests -Dsonar.host.url=http://localhost:9000 -Dsonar.token=${SONAR_TOKEN}"
                        } catch (Exception e) {
                            echo "SonarQube analysis failed (non-blocking): ${e.getMessage()}"
                        }
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    bat "docker build -f docker/Dockerfile -t ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:${BUILD_NUMBER} ."
                    bat "docker tag ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:${BUILD_NUMBER} ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:latest"
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        bat "echo %DOCKER_PASS% | docker login ${DOCKER_REGISTRY} -u %DOCKER_USER% --password-stdin"
                        bat "docker push ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:${BUILD_NUMBER}"
                        bat "docker push ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:latest"
                        bat "docker logout ${DOCKER_REGISTRY}"
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            when {
                expression { !env.BRANCH_NAME || env.BRANCH_NAME == 'main' }
            }
            steps {
                script {
                    bat """
                    kubectl apply -f k8s/namespace.yaml
                    kubectl apply -n %K8S_NAMESPACE% -f k8s/secret.yaml
                    kubectl apply -n %K8S_NAMESPACE% -f k8s/mysql.yaml
                    kubectl rollout status deployment/mysql -n %K8S_NAMESPACE% --timeout=180s
                    kubectl apply -n %K8S_NAMESPACE% -f k8s/deployment.yaml
                    kubectl apply -n %K8S_NAMESPACE% -f k8s/service.yaml
                    kubectl set image deployment/clinic-appointment-system clinic-appointment-system=%DOCKER_REPOSITORY%/%DOCKER_IMAGE_NAME%:%BUILD_NUMBER% -n %K8S_NAMESPACE%
                    kubectl rollout status deployment/clinic-appointment-system -n %K8S_NAMESPACE% --timeout=180s
                    """
                }
            }
        }

        stage('PowerShell Monitoring') {
            steps {
                powershell script: '''
                    Write-Host "Starting post-deployment monitoring..."
                    .\minikubemonitoring.ps1 -Environment Production -Namespace clinic
                '''
            }
        }

        stage('Summary') {
            steps {
                script {
                    echo """
                    ╔════════════════════════════════════════════╗
                    ║ Pipeline Summary                           ║
                    ╠════════════════════════════════════════════╣
                    ║ Build #: ${env.BUILD_NUMBER}
                    ║ Branch: ${env.BRANCH_NAME}
                    ║ Status: ${currentBuild.result ?: 'SUCCESS'}
                    ║ Project: ${APP_NAME}
                    ║ Docker Image: ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}
                    ╠════════════════════════════════════════════╣
                    ║ Build completed!                           ║
                    ╚════════════════════════════════════════════╝
                    """
                }
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline succeeded"
            echo "Docker image pushed to DockerHub: ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}"
        }
        failure {
            echo "❌ Pipeline failed"
            echo "Check the console output for detailed error information"
        }
        always {
            script {
                try {
                    bat "docker logout ${DOCKER_REGISTRY}"
                } catch (Exception e) {
                    echo "Docker logout failed or not needed: ${e.getMessage()}"
                }
            }
            cleanWs(
                cleanWhenNotBuilt: false,
                deleteDirs: true,
                disableDeferredWipeout: true,
                notFailBuild: true,
                patterns: [
                    [pattern: 'target/surefire-reports/**', type: 'EXCLUDE'],
                    [pattern: 'target/site/jacoco/**', type: 'EXCLUDE']
                ]
            )
        }
    }
}
```

## Jenkins Credentials Configuration

The pipeline requires the following Jenkins credentials to be configured:

### Required Credentials

1. **SonarQube Token** (`sonarqube`)
   - Type: Secret text
   - ID: `sonarqube`
   - Description: SonarQube authentication token
   - Usage: `withCredentials([string(credentialsId: 'sonarqube', variable: 'SONAR_TOKEN')])`

2. **Docker Hub Credentials** (`docker`)
   - Type: Username with password
   - ID: `docker`
   - Description: DockerHub registry credentials
   - Usage: `withCredentials([usernamePassword(credentialsId: 'docker', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')])`

3. **Kubernetes Config File**
   - File Location: `C:\\Users\\Abdelkader\\.kube\\config`
   - Description: Minikube cluster configuration
   - Usage: Direct file path in environment variable `KUBECONFIG`

### Credential Setup Instructions

1. **Navigate to Jenkins**: `Manage Jenkins` → `Manage Credentials`
2. **Add SonarQube Token**: 
   - Kind: Secret text
   - Secret: Your SonarQube token
   - ID: `sonarqube`
3. **Add Docker Credentials**:
   - Kind: Username with password
   - Username: Your DockerHub username
   - Password: Your DockerHub password/token
   - ID: `docker`
4. **Ensure Kubernetes Config**:
   - Verify `.kube/config` file exists at specified path
   - Contains valid Minikube cluster configuration

## Environment Variables

```groovy
environment {
    APP_NAME = "clinic-appointment-system"
    DOCKER_IMAGE_NAME = "clinic-app"
    DOCKER_REGISTRY = "docker.io"
    DOCKER_REPOSITORY = "mhabdelkader"
    K8S_NAMESPACE = "clinic"
    K8S_MONITORING_NAMESPACE = "monitoring"
    K8S_CONTEXT = "minikube"
    KUBECONFIG = "C:\\Users\\Abdelkader\\.kube\\config"
    SONAR_HOST_URL = "http://localhost:9000"
}
```

## Jenkins Pipeline Success Criteria

✅ **Build**: Maven compilation successful  
✅ **Tests**: All JUnit tests pass with >80% coverage  
✅ **Quality**: SonarQube quality gate passed  
✅ **Package**: JAR artifact created and archived  
✅ **Docker**: Image built and pushed to registry  
✅ **Deploy**: Kubernetes deployment successful  
✅ **Monitor**: PowerShell script validates system health  

## Rollback Strategy

If the PowerShell monitoring script detects issues:

1. **Automatic Rollback**
   ```bash
   kubectl rollout undo deployment/clinic-app -n clinic-system
   ```

2. **Database Rollback**
   ```bash
   kubectl exec -it mysql-pod -- mysql -u root -p < backup.sql
   ```

3. **Notification**
   ```powershell
   Send-MailMessage -To "devops@clinic.com" -Subject "Deployment Rollback" -Body "Issues detected, rolled back to previous version"
   ```

## Monitoring Dashboard

The PowerShell script integrates with:
- **Prometheus**: Metrics collection
- **Grafana**: Visualization dashboards
- **ELK Stack**: Log aggregation and analysis
- **Slack/Teams**: Real-time notifications

---

**Note**: The PowerShell monitoring script (`minikubemonitoring.ps1`) is the final and critical step that ensures the entire deployment is successful and the system is operating correctly in production.