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
            } else {
              echo "No JUnit test reports found to publish"
            }
          }
          script {
            if (fileExists('target/site/jacoco/jacoco.xml')) {
              try {
                recordCoverage(
                  tools: [[
                    pattern: 'target/site/jacoco/jacoco.xml',
                    type: 'JACOCO'
                  ]]
                )
                echo "Coverage published successfully"
              } catch (Exception e) {
                echo "Coverage publish failed (check plugin): ${e.message}"
              }
            } else {
              echo "No JaCoCo XML report found"
            }
          }
        }
      }
    }

    stage('SonarQube Analysis') {
      steps {
        script {
          echo "Running SonarQube analysis on local instance..."
          withCredentials([string(credentialsId: 'sonarqube', variable: 'SONAR_TOKEN')]) {
          def sonarQubeScannerParams = [
            'sonar.host.url'                       : 'http://localhost:9000',
            'sonar.token'                          : env.SONAR_TOKEN,
            'sonar.projectKey'                     : 'clinic',
            'sonar.projectName'                    : 'clinic-appointment-system',
            'sonar.sources'                        : 'src/main/java',
            'sonar.tests'                          : 'src/test/java',
            'sonar.java.source'                    : '21',
            'sonar.java.target'                    : '21',
            'sonar.java.test.binaries'             : 'target/test-classes',
            'sonar.junit.reportsPath'              : 'target/surefire-reports',
            'sonar.coverage.jacoco.xmlReportPaths' : 'target/site/jacoco/jacoco.xml',
            'sonar.java.coveragePlugin'            : 'jacoco'
          ]

          if (env.CHANGE_ID) {
            sonarQubeScannerParams['sonar.pullrequest.key'] = env.CHANGE_ID
            sonarQubeScannerParams['sonar.pullrequest.branch'] = env.CHANGE_BRANCH
            sonarQubeScannerParams['sonar.pullrequest.base'] = env.CHANGE_TARGET
            echo "PR Analysis: Decorating PR #${env.CHANGE_ID}"
          } 
          else if (env.BRANCH_NAME && env.BRANCH_NAME != 'main') {
            sonarQubeScannerParams['sonar.branch.name'] = env.BRANCH_NAME
            echo "Branch Analysis: ${env.BRANCH_NAME}"
          } else {
            echo "Main/Default Branch Analysis"
          }

          def sonarScannerArgs = sonarQubeScannerParams.collect { k, v ->
            "-D${k}=${v}"
          }.join(' ')

          try {
            bat "mvn sonar:sonar -DskipTests ${sonarScannerArgs}"
            echo "SonarQube analysis completed successfully"
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
          echo "Building Docker image: ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}"
          bat "docker build -f docker/Dockerfile -t ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER} ."
          bat "docker tag ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER} ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:latest"
          bat "docker images ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}"
        }
      }
    }

    stage('Push Docker Image') {
      steps {
        script {
          withCredentials([usernamePassword(credentialsId: 'docker', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
            echo "Logging into Docker registry: ${DOCKER_REGISTRY}"
            bat "echo %DOCKER_PASS% | docker login ${DOCKER_REGISTRY} -u %DOCKER_USER% --password-stdin"
            bat "docker push ${DOCKER_REPOSITORY}/${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}"
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
set KUBECONFIG=C:\\Users\\Abdelkader\\.kube\\config

echo === Checking Kubernetes cluster ===
kubectl cluster-info || (echo Cluster unreachable! && exit /b 1)

echo === Apply namespace ===
kubectl apply -f k8s/namespace.yaml || exit /b 1

echo === Apply secrets and MySQL resources ===
kubectl apply -n %K8S_NAMESPACE% -f k8s/secret.yaml || exit /b 1
kubectl apply -n %K8S_NAMESPACE% -f k8s/mysql.yaml || exit /b 1

echo === Wait for MySQL rollout ===
kubectl rollout status deployment/mysql -n %K8S_NAMESPACE% --timeout=240 || exit /b 1

echo === Apply application manifests ===
kubectl apply -n %K8S_NAMESPACE% -f k8s/deployment.yaml || exit /b 1
kubectl apply -n %K8S_NAMESPACE% -f k8s/service.yaml || exit /b 1
kubectl apply -n %K8S_NAMESPACE% -f k8s/ingress.yaml || exit /b 1

echo === Update application image ===
kubectl set image deployment/clinic-appointment-system clinic-appointment-system=%DOCKER_REPOSITORY%/%DOCKER_IMAGE_NAME%:%BUILD_NUMBER% -n %K8S_NAMESPACE% || exit /b 1

echo === Wait for rollout ===
kubectl rollout status deployment/clinic-appointment-system -n %K8S_NAMESPACE% --timeout=180s || exit /b 1

echo === Show current state ===
kubectl get svc clinic-appointment-system -n %K8S_NAMESPACE% -o wide
kubectl get pods -n %K8S_NAMESPACE%
kubectl get nodes -o wide
"""
    }
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
          echo "Ensuring Docker logout from ${DOCKER_REGISTRY}"
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
