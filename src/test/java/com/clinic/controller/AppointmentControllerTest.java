package com.clinic.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinic.dto.AppointmentRequest;
import com.clinic.dto.AppointmentResponse;
import com.clinic.model.AppointmentStatus;
import com.clinic.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web layer tests for {@link AppointmentController} using MockMvc.
 */
@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private AppointmentResponse testResponse;
    private AppointmentRequest testRequest;

    @BeforeEach
    void setUp() {
        testResponse = new AppointmentResponse();
        testResponse.setId(1L);
        testResponse.setPatientName("John Doe");
        testResponse.setDoctorName("Dr. Smith");
        testResponse.setAppointmentDate(LocalDateTime.now().plusDays(1));
        testResponse.setStatus(AppointmentStatus.SCHEDULED);
        testRequest = new AppointmentRequest();
        testRequest.setPatientId(1L);
        testRequest.setDoctorId(1L);
        testRequest.setAppointmentDate(LocalDateTime.now().plusDays(1));
        testRequest.setNotes("Checkup");
    }

    @Test
    void testGetAllAppointments() throws Exception {
        List<AppointmentResponse> list = Arrays.asList(testResponse);
        when(appointmentService.getAllAppointments()).thenReturn(list);
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientName").value("John Doe"))
                .andExpect(jsonPath("$[0].doctorName").value("Dr. Smith"));
    }

    @Test
    void testCreateAppointment() throws Exception {
        when(appointmentService.createAppointment(any(AppointmentRequest.class))).thenReturn(testResponse);
        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientName").value("John Doe"))
                .andExpect(jsonPath("$.doctorName").value("Dr. Smith"));
    }

    @Test
    void testUpdateAppointmentStatus() throws Exception {
        when(appointmentService.updateAppointmentStatus(1L, AppointmentStatus.CONFIRMED))
                .thenReturn(testResponse);
        mockMvc.perform(put("/api/appointments/1/status")
                .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientName").value("John Doe"));
    }
}