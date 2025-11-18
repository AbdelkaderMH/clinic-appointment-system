package com.clinic.service;

import java.time.LocalDateTime;
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

import com.clinic.dto.AppointmentRequest;
import com.clinic.dto.AppointmentResponse;
import com.clinic.model.Appointment;
import com.clinic.model.Doctor;
import com.clinic.model.Patient;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.PatientRepository;

/**
 * Unit tests for {@link AppointmentService} using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @InjectMocks
    private AppointmentService appointmentService;

    private Patient testPatient;
    private Doctor testDoctor;
    private Appointment testAppointment;
    private AppointmentRequest testRequest;

    @BeforeEach
    void setUp() {
        testPatient = new Patient("John Doe", "john@example.com", "1234567890", "No allergies");
        testPatient.setId(1L);
        testDoctor = new Doctor("Dr. Smith", "Cardiology", "CARD123", "smith@clinic.com");
        testDoctor.setId(1L);
        testAppointment = new Appointment(testPatient, testDoctor,
                LocalDateTime.now().plusDays(1), "Checkup");
        testAppointment.setId(1L);
        testRequest = new AppointmentRequest();
        testRequest.setPatientId(1L);
        testRequest.setDoctorId(1L);
        testRequest.setAppointmentDate(LocalDateTime.now().plusDays(1));
        testRequest.setNotes("Checkup");
    }

    @Test
    void testGetAllAppointments() {
        when(appointmentRepository.findAll()).thenReturn(Arrays.asList(testAppointment));
        List<AppointmentResponse> result = appointmentService.getAllAppointments();
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getPatientName());
        verify(appointmentRepository, times(1)).findAll();
    }

    @Test
    void testCreateAppointmentSuccess() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.findByDoctorAndDateRange(any(), any(), any())).thenReturn(Arrays.asList());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);
        AppointmentResponse result = appointmentService.createAppointment(testRequest);
        assertNotNull(result);
        assertEquals("John Doe", result.getPatientName());
        assertEquals("Dr. Smith", result.getDoctorName());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void testCreateAppointmentPatientNotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> appointmentService.createAppointment(testRequest));
        assertTrue(ex.getMessage().contains("Patient not found"));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testCreateAppointmentDoctorNotAvailable() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.findByDoctorAndDateRange(any(), any(), any())).thenReturn(Arrays.asList(testAppointment));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> appointmentService.createAppointment(testRequest));
        assertTrue(ex.getMessage().contains("not available"));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }
}