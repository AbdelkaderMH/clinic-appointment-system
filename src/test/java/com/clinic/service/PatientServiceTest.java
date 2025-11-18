package com.clinic.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.clinic.model.Patient;
import com.clinic.repository.PatientRepository;

/**
 * Unit tests for {@link PatientService} using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testPatient = new Patient("John Doe", "john@example.com", "1234567890", "No allergies");
        testPatient.setId(1L);
    }

    @Test
    void testGetAllPatients() {
        when(patientRepository.findAll()).thenReturn(Arrays.asList(testPatient));
        List<Patient> result = patientService.getAllPatients();
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void testGetPatientById() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        Optional<Patient> result = patientService.getPatientById(1L);
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    void testCreatePatientSuccess() {
        when(patientRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(patientRepository.findByPhone(any())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);
        Patient saved = patientService.createPatient(testPatient);
        assertNotNull(saved);
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void testCreatePatientDuplicateEmail() {
        when(patientRepository.findByEmail(any())).thenReturn(Optional.of(testPatient));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> patientService.createPatient(testPatient));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testUpdatePatientNotFound() {
        when(patientRepository.findById(any())).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> patientService.updatePatient(1L, testPatient));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void testDeletePatientSuccess() {
        when(patientRepository.existsById(1L)).thenReturn(true);
        patientService.deletePatient(1L);
        verify(patientRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletePatientNotFound() {
        when(patientRepository.existsById(1L)).thenReturn(false);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> patientService.deletePatient(1L));
        assertTrue(ex.getMessage().contains("not found"));
        verify(patientRepository, never()).deleteById(any());
    }
}