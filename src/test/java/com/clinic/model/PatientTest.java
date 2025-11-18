package com.clinic.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PatientTest {

    @Test
    void testPatientCreation() {
        Patient patient = new Patient("John Doe", "john@example.com", "1234567890", "No allergies");
        
        assertEquals("John Doe", patient.getName());
        assertEquals("john@example.com", patient.getEmail());
        assertEquals("1234567890", patient.getPhone());
        assertEquals("No allergies", patient.getMedicalHistory());
    }

    @Test
    void testPatientSettersAndGetters() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setName("Jane Doe");
        patient.setEmail("jane@example.com");
        patient.setPhone("0987654321");
        patient.setMedicalHistory("Diabetes");

        assertEquals(1L, patient.getId());
        assertEquals("Jane Doe", patient.getName());
        assertEquals("jane@example.com", patient.getEmail());
        assertEquals("0987654321", patient.getPhone());
        assertEquals("Diabetes", patient.getMedicalHistory());
    }

    @Test
    void testPatientEqualsAndHashCode() {
        Patient patient1 = new Patient("John Doe", "john@example.com", "1234567890", "No allergies");
        patient1.setId(1L);
        
        Patient patient2 = patient1; // Same reference

        assertEquals(patient1, patient2);
        assertEquals(patient1.hashCode(), patient2.hashCode());
    }

    @Test
    void testPatientToString() {
        Patient patient = new Patient("John Doe", "john@example.com", "1234567890", "No allergies");
        patient.setId(1L);
        
        String toString = patient.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Patient"));
    }
}