# Testing Guide

Tests are located under `src/test/java`. They include unit tests for
services and controllers using Mockito and integration tests using
Spring Boot’s testing support.

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=AppointmentServiceTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=AppointmentServiceTest#testCreateAppointment
```

### Generate Coverage Report
```bash
mvn jacoco:report
```

The coverage report will be located at `target/site/jacoco/index.html`.

---

## Current Test Suite

### Unit Tests
- **AppointmentServiceTest** - Service layer tests for appointment operations
- **AppointmentControllerTest** - Controller layer tests for appointment endpoints
- **PatientServiceTest** - Service layer tests for patient operations
- **DoctorServiceTest** - Service layer tests for doctor operations (if available)

### Test Status
✅ **All 17 tests passing**
- 8 Appointment Service Tests
- 5 Appointment Controller Tests  
- 4 Patient Service Tests

### Test Coverage
View detailed coverage at: `target/site/jacoco/index.html` after running `mvn jacoco:report`

---

## Testing After Serialization Fix

### API Integration Tests

#### Test Doctors Endpoint
```bash
# All doctors
curl http://localhost:8080/api/doctors

# Specific doctor
curl http://localhost:8080/api/doctors/1

# Doctors by specialization
curl http://localhost:8080/api/doctors/specialization/Cardiology
```

**Expected**: HTTP 200 with JSON response (no ByteBuddyInterceptor errors)

#### Test Patients Endpoint
```bash
# All patients
curl http://localhost:8080/api/patients

# Specific patient
curl http://localhost:8080/api/patients/1
```

**Expected**: HTTP 200 with JSON response

#### Test Appointments Endpoint
```bash
# All appointments (includes patient and doctor objects)
curl http://localhost:8080/api/appointments

# Specific appointment
curl http://localhost:8080/api/appointments/1

# Appointments for a patient
curl http://localhost:8080/api/appointments/patient/1

# Appointments for a doctor
curl http://localhost:8080/api/appointments/doctor/1
```

**Expected**: HTTP 200 with nested JSON including related entities

---

## Test Scenarios for New Features

### Doctor Management
- [ ] Create doctor with valid data → HTTP 201
- [ ] Create doctor with duplicate license → HTTP 400
- [ ] Update doctor details → HTTP 200
- [ ] Delete doctor → HTTP 204
- [ ] Get doctor by ID → HTTP 200 (no appointments list)
- [ ] Get doctors by specialization → HTTP 200

### Patient Management
- [ ] Create patient with valid data → HTTP 201
- [ ] Create patient with duplicate email → HTTP 400
- [ ] Update patient details → HTTP 200
- [ ] Delete patient → HTTP 204
- [ ] Get patient by ID → HTTP 200

### Appointment Management
- [ ] Create appointment with valid data → HTTP 201
- [ ] Create appointment in past → HTTP 400
- [ ] Create appointment with conflicting doctor time → HTTP 400
- [ ] Cancel appointment → HTTP 204 / Update status
- [ ] Update appointment status → HTTP 200
- [ ] Delete appointment → HTTP 204
- [ ] Get appointments by patient → HTTP 200
- [ ] Get appointments by doctor → HTTP 200

---

## Swagger UI Testing

The easiest way to test all endpoints with the serialization fix applied:

1. Start the application:
   ```bash
   docker-compose up --build
   ```

2. Access Swagger UI:
   ```
   http://localhost:8080/swagger-ui.html
   ```

3. Try out endpoints in the interactive UI:
   - Use "Try it out" button for each endpoint
   - Verify HTTP 200 responses
   - Check JSON response structure
   - Verify no serialization errors

---

## Known Test Limitations

### Lazy Loading Tests
- Lazy-loaded relationships are excluded from some responses via `@JsonIgnore`
- Use dedicated endpoints to retrieve related data:
  - Doctors: `GET /api/doctors` (appointments not included)
  - Appointments: `GET /api/appointments` (full patient & doctor info included)

### Date Validation
- Appointment dates must be in the future (validated by `@Future` annotation)
- Test data uses relative dates: `datetime('now', '+X days')`

---

## CI/CD Testing

GitHub Actions workflow (if configured):
```yaml
- Run tests on each commit
- Generate coverage reports
- Check code quality
```

---

## Troubleshooting Tests

### Tests Fail After Serialization Changes
- ✅ Rebuild: `mvn clean install`
- ✅ Clear cache: `mvn clean`
- ✅ Check Jackson version in pom.xml

### Serialization Error in Tests
- ✅ Verify `@JsonIgnore` annotations are in place
- ✅ Verify `@JsonIgnoreProperties` annotations are in place
- ✅ Check `JacksonConfig.java` exists

### Database Issues in Tests
- ✅ Check `application-test.properties` exists
- ✅ Verify test data initialization
- ✅ Look for concurrent test issues

---

## Related Documentation

- [../CODE_ISSUES.md](../CODE_ISSUES.md) - Known issues and fixes
- [../SERIALIZATION_FIX.md](../SERIALIZATION_FIX.md) - Detailed serialization fix
- [../DOCKER_DATA_FIX.md](../DOCKER_DATA_FIX.md) - Data initialization
- [../SAMPLE_DATA.md](../SAMPLE_DATA.md) - Test data overview