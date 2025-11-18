# API Documentation

## Base URL

All endpoints are relative to `http://localhost:8090/api`.

## Patients

### Get All Patients

`GET /api/patients`

Response:

```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "1234567890",
    "medicalHistory": "No known allergies"
  }
]
```

### Create Patient

`POST /api/patients`

Request Body:

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "medicalHistory": "No allergies"
}
```

Response: 201 Created

### Get Patient by ID

`GET /api/patients/{id}`

## Doctors

### Get All Doctors

`GET /api/doctors`

### Create Doctor

`POST /api/doctors`

Request Body:

```json
{
  "name": "Dr. Sarah Williams",
  "specialization": "Cardiology",
  "licenseNumber": "CARD001",
  "email": "sarah@clinic.com"
}
```

## Appointments

### Create Appointment

`POST /api/appointments`

Request Body:

```json
{
  "patientId": 1,
  "doctorId": 1,
  "appointmentDate": "2024-12-01T10:00:00",
  "notes": "Regular checkup"
}
```

Response: 201 Created

### Update Appointment Status

`PUT /api/appointments/{id}/status?status=CONFIRMED`

Valid status values: `SCHEDULED`, `CONFIRMED`, `COMPLETED`, `CANCELLED`.

## Error Responses

All error responses follow this format:

```json
{
  "timestamp": "2024-11-13T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/patients"
}
```

## HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Successful GET/PUT request |
| 201 | Successful POST request |
| 204 | Successful DELETE request |
| 400 | Bad Request – validation error |
| 404 | Not Found – resource not found |
| 500 | Internal Server Error |