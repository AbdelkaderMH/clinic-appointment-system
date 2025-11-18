package com.clinic.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AppointmentTest {

    @Test
    void testAppointmentCreation() {
        Patient patient = new Patient("John Doe", "john@example.com", "1234567890", "No allergies");
        Doctor doctor = new Doctor("Dr. Smith", "Cardiology", "smith@clinic.com", "1234567890");
        LocalDateTime dateTime = LocalDateTime.of(2024, 12, 25, 10, 0);
        
        Appointment appointment = new Appointment(patient, doctor, dateTime, "Checkup");
        
        assertEquals(patient, appointment.getPatient());
        assertEquals(doctor, appointment.getDoctor());
        assertEquals(dateTime, appointment.getAppointmentDate());
        assertEquals("Checkup", appointment.getNotes());
        assertEquals(AppointmentStatus.SCHEDULED, appointment.getStatus());
    }

    @Test
    void testAppointmentSettersAndGetters() {
        Appointment appointment = new Appointment();
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        LocalDateTime dateTime = LocalDateTime.of(2024, 12, 25, 10, 0);
        
        appointment.setId(1L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(dateTime);
        appointment.setNotes("Follow-up");
        appointment.setStatus(AppointmentStatus.COMPLETED);

        assertEquals(1L, appointment.getId());
        assertEquals(patient, appointment.getPatient());
        assertEquals(doctor, appointment.getDoctor());
        assertEquals(dateTime, appointment.getAppointmentDate());
        assertEquals("Follow-up", appointment.getNotes());
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
    }

    @Test
    void testAppointmentStatusEnum() {
        assertEquals("SCHEDULED", AppointmentStatus.SCHEDULED.toString());
        assertEquals("COMPLETED", AppointmentStatus.COMPLETED.toString());
        assertEquals("CANCELLED", AppointmentStatus.CANCELLED.toString());
    }
}