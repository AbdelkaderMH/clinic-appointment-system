package com.clinic.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DoctorTest {

    @Test
    void testDoctorCreation() {
        Doctor doctor = new Doctor("Dr. Smith", "Cardiology", "LIC123", "smith@clinic.com");
        
        assertEquals("Dr. Smith", doctor.getName());
        assertEquals("Cardiology", doctor.getSpecialization());
        assertEquals("LIC123", doctor.getLicenseNumber());
        assertEquals("smith@clinic.com", doctor.getEmail());
    }

    @Test
    void testDoctorSettersAndGetters() {
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Dr. Johnson");
        doctor.setSpecialization("Neurology");
        doctor.setEmail("johnson@clinic.com");
        doctor.setLicenseNumber("LIC456");

        assertEquals(1L, doctor.getId());
        assertEquals("Dr. Johnson", doctor.getName());
        assertEquals("Neurology", doctor.getSpecialization());
        assertEquals("johnson@clinic.com", doctor.getEmail());
        assertEquals("LIC456", doctor.getLicenseNumber());
    }

    @Test
    void testDoctorEqualsAndHashCode() {
        Doctor doctor1 = new Doctor("Dr. Smith", "Cardiology", "LIC123", "smith@clinic.com");
        doctor1.setId(1L);
        
        Doctor doctor2 = doctor1; // Same reference

        assertEquals(doctor1, doctor2);
        assertEquals(doctor1.hashCode(), doctor2.hashCode());
    }

    @Test
    void testDoctorToString() {
        Doctor doctor = new Doctor("Dr. Smith", "Cardiology", "LIC123", "smith@clinic.com");
        doctor.setId(1L);
        
        String toString = doctor.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Doctor"));
    }
}