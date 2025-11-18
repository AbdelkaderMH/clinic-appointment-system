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

import com.clinic.exception.BusinessException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.model.Doctor;
import com.clinic.repository.DoctorRepository;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor testDoctor;

    @BeforeEach
    void setUp() {
        testDoctor = new Doctor("Dr. Smith", "Cardiology", "CARD123", "smith@clinic.com");
        testDoctor.setId(1L);
    }

    @Test
    void testGetAllDoctors() {
        when(doctorRepository.findAll()).thenReturn(Arrays.asList(testDoctor));
        List<Doctor> result = doctorService.getAllDoctors();
        assertEquals(1, result.size());
        assertEquals("Dr. Smith", result.get(0).getName());
        verify(doctorRepository, times(1)).findAll();
    }

    @Test
    void testGetDoctorById() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        Optional<Doctor> result = doctorService.getDoctorById(1L);
        assertTrue(result.isPresent());
        assertEquals("Dr. Smith", result.get().getName());
        verify(doctorRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateDoctorSuccess() {
        when(doctorRepository.findByLicenseNumber(any())).thenReturn(Optional.empty());
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);
        Doctor saved = doctorService.createDoctor(testDoctor);
        assertNotNull(saved);
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    @Test
    void testCreateDoctorDuplicateLicense() {
        when(doctorRepository.findByLicenseNumber(any())).thenReturn(Optional.of(testDoctor));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> doctorService.createDoctor(testDoctor));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void testGetDoctorsBySpecialization() {
        when(doctorRepository.findBySpecialization("Cardiology")).thenReturn(Arrays.asList(testDoctor));
        List<Doctor> result = doctorService.getDoctorsBySpecialization("Cardiology");
        assertEquals(1, result.size());
        assertEquals("Cardiology", result.get(0).getSpecialization());
        verify(doctorRepository, times(1)).findBySpecialization("Cardiology");
    }

    @Test
    void testUpdateDoctorSuccess() {
        Doctor updatedDetails = new Doctor("Dr. Updated", "Neurology", "CARD123", "updated@clinic.com");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);
        Doctor result = doctorService.updateDoctor(1L, updatedDetails);
        assertNotNull(result);
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    @Test
    void testUpdateDoctorNotFound() {
        when(doctorRepository.findById(any())).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> doctorService.updateDoctor(1L, testDoctor));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void testDeleteDoctorSuccess() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        doctorService.deleteDoctor(1L);
        verify(doctorRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteDoctorNotFound() {
        when(doctorRepository.existsById(1L)).thenReturn(false);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> doctorService.deleteDoctor(1L));
        assertTrue(ex.getMessage().contains("not found"));
        verify(doctorRepository, never()).deleteById(any());
    }
}