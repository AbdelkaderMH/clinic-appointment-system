#!/bin/bash
# Build script for Clinic Appointment System

set -e

trap 'echo "❌ Build failed"; exit 1' ERR

echo "🚀 Building Clinic Appointment System..."

echo "🧹 Cleaning previous builds..."
mvn clean || { echo "Maven clean failed"; exit 1; }

echo "🧪 Running tests..."
mvn test || { echo "Tests failed"; exit 1; }

echo "📊 Generating coverage report..."
mvn jacoco:report || { echo "Coverage report generation failed"; exit 1; }

echo "🔨 Building application..."
mvn package -DskipTests || { echo "Package build failed"; exit 1; }

if command -v docker &> /dev/null; then
    echo "🐳 Building Docker image..."
    docker build -t clinic-appointment-system:latest -f docker/Dockerfile . || { echo "Docker build failed"; exit 1; }
fi

echo "✅ Build completed successfully!"
echo ""
echo "📋 Build artifacts:"
echo "  - JAR file: target/clinic-appointment-system-*.jar"
echo "  - Coverage report: target/site/jacoco/index.html"
if command -v docker &> /dev/null; then
    echo "  - Docker image: clinic-appointment-system:latest"
fi
