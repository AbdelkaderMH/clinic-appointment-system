package com.clinic.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinic.model.Doctor;
import com.clinic.service.DoctorService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(DoctorController.class)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorService doctorService;

    @Autowired
    private ObjectMapper objectMapper;

    private Doctor testDoctor;

    @BeforeEach
    void setUp() {
        testDoctor = new Doctor("Dr. Smith", "Cardiology", "CARD123", "smith@clinic.com");
        testDoctor.setId(1L);
    }

    @Test
    void testGetAllDoctors() throws Exception {
        List<Doctor> doctors = Arrays.asList(testDoctor);
        when(doctorService.getAllDoctors()).thenReturn(doctors);
        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dr. Smith"))
                .andExpect(jsonPath("$[0].specialization").value("Cardiology"));
    }

    @Test
    void testGetDoctorById() throws Exception {
        when(doctorService.getDoctorById(1L)).thenReturn(Optional.of(testDoctor));
        mockMvc.perform(get("/api/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dr. Smith"))
                .andExpect(jsonPath("$.specialization").value("Cardiology"));
    }

    @Test
    void testGetDoctorByIdNotFound() throws Exception {
        when(doctorService.getDoctorById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/doctors/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetDoctorsBySpecialization() throws Exception {
        List<Doctor> doctors = Arrays.asList(testDoctor);
        when(doctorService.getDoctorsBySpecialization("Cardiology")).thenReturn(doctors);
        mockMvc.perform(get("/api/doctors/specialization/Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].specialization").value("Cardiology"));
    }

    @Test
    void testCreateDoctor() throws Exception {
        when(doctorService.createDoctor(any(Doctor.class))).thenReturn(testDoctor);
        mockMvc.perform(post("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDoctor)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dr. Smith"));
    }

    @Test
    void testUpdateDoctor() throws Exception {
        when(doctorService.updateDoctor(eq(1L), any(Doctor.class))).thenReturn(testDoctor);
        mockMvc.perform(put("/api/doctors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDoctor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dr. Smith"));
    }

    @Test
    void testDeleteDoctor() throws Exception {
        doNothing().when(doctorService).deleteDoctor(1L);
        mockMvc.perform(delete("/api/doctors/1"))
                .andExpect(status().isNoContent());
    }
}