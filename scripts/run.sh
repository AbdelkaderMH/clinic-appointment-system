#!/bin/bash
# Run the Clinic Appointment System application

set -e

trap 'echo "❌ Application startup failed"; exit 1' ERR

echo "🚀 Starting Clinic Appointment System..."
mvn spring-boot:run || { echo "Spring Boot startup failed"; exit 1; }
