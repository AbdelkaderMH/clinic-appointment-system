#!/bin/bash
# Run the test suite for the Clinic Appointment System

set -e

echo "🧪 Running all tests..."
mvn test
echo "✅ Tests completed."