package com.hospital.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.system.model.Booking;
import com.hospital.system.model.Equipment;
import com.hospital.system.model.EquipmentStatus;
import com.hospital.system.model.Priority;
import com.hospital.system.repository.BookingRepository;
import com.hospital.system.repository.EquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for HospitalController.
 * Uses @SpringBootTest with real database (H2 in-memory for tests).
 * Tests full request/response cycle with actual service and repository layers.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HospitalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Long mriId;
    private Long ctId;

    @BeforeEach
    void setUp() {
        // Clear and setup test data
        bookingRepository.deleteAll();
        equipmentRepository.deleteAll();

        // Create test equipment
        Equipment mri = new Equipment();
        mri.setName("MRI-Test");
        mri.setType("MRI");
        mri.setStatus(EquipmentStatus.AVAILABLE);
        mri.setBufferTime(60);
        mri = equipmentRepository.save(mri);
        mriId = mri.getId();

        Equipment ct = new Equipment();
        ct.setName("CT-Test");
        ct.setType("CT");
        ct.setStatus(EquipmentStatus.AVAILABLE);
        ct.setBufferTime(30);
        ct = equipmentRepository.save(ct);
        ctId = ct.getId();
    }

    @Test
    @DisplayName("GET /api/equipment - Should return all equipment with queue info")
    void shouldGetAllEquipment() throws Exception {
        mockMvc.perform(get("/api/equipment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].queueLength").exists())
                .andExpect(jsonPath("$[0].nextAvailable").exists());
    }

    @Test
    @DisplayName("POST /api/bookings - Should create a new booking")
    void shouldCreateBooking() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "patientName", "Integration Test Patient",
                "equipmentId", mriId.intValue(),
                "slotTime", "2026-12-31T10:00",
                "requestedPriority", "NORMAL"
        );

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.patientName").value("Integration Test Patient"))
                .andExpect(jsonPath("$.equipmentId").value(mriId.intValue()))
                .andExpect(jsonPath("$.priority").value("NORMAL"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/bookings/pending - Should return pending bookings")
    void shouldGetPendingBookings() throws Exception {
        // Given - Create pending bookings
        createBooking("Pending Patient 1", mriId, Priority.NORMAL, "PENDING");
        createBooking("Pending Patient 2", ctId, Priority.EMERGENCY, "PENDING");
        createBooking("Confirmed Patient", mriId, Priority.NORMAL, "CONFIRMED");

        // When & Then
        mockMvc.perform(get("/api/bookings/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].status", everyItem(equalTo("PENDING"))));
    }

    @Test
    @DisplayName("POST /api/bookings/{id}/confirm - Should confirm booking with priority")
    void shouldConfirmBooking() throws Exception {
        // Given - Create a pending booking
        Booking pending = createBooking("To Confirm", mriId, Priority.NORMAL, "PENDING");

        Map<String, String> request = Map.of("assignedPriority", "EMERGENCY");

        // When & Then
        mockMvc.perform(post("/api/bookings/{id}/confirm", pending.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pending.getId()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.priority").value("EMERGENCY"));
    }

    @Test
    @DisplayName("GET /api/queue/{equipmentId} - Should return sorted queue for equipment")
    void shouldGetQueueForEquipment() throws Exception {
        // Given - Create confirmed bookings with different priorities
        createBooking("Normal Patient", mriId, Priority.NORMAL, "CONFIRMED");
        createBooking("Emergency Patient", mriId, Priority.EMERGENCY, "CONFIRMED");

        // When & Then - Emergency should come first due to priority
        mockMvc.perform(get("/api/queue/{equipmentId}", mriId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].priority").value("EMERGENCY"))
                .andExpect(jsonPath("$[1].priority").value("NORMAL"));
    }

    @Test
    @DisplayName("POST /api/queue/{equipmentId}/next - Should call next patient")
    void shouldCallNextPatient() throws Exception {
        // Given - Create confirmed booking
        createBooking("Next Patient", mriId, Priority.NORMAL, "CONFIRMED");

        // When & Then
        mockMvc.perform(post("/api/queue/{equipmentId}/next", mriId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientName").value("Next Patient"))
                .andExpect(jsonPath("$.status").value("IN_USE"));
    }

    @Test
    @DisplayName("POST /api/bookings/{id}/serve - Should mark patient as served")
    void shouldServePatient() throws Exception {
        // Given - Create booking in IN_USE status
        Booking inUse = createBooking("Serving Patient", mriId, Priority.NORMAL, "IN_USE");

        // When & Then
        mockMvc.perform(post("/api/bookings/{id}/serve", inUse.getId()))
                .andExpect(status().isOk());

        // Verify booking is now SERVED
        mockMvc.perform(get("/api/queue/{equipmentId}", mriId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0))); // SERVED bookings not in queue
    }

    @Test
    @DisplayName("POST /api/bookings - Should default to NORMAL priority when invalid priority provided")
    void shouldDefaultToNormalPriority() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "patientName", "Invalid Priority Patient",
                "equipmentId", mriId.intValue(),
                "slotTime", "2026-12-31T10:00",
                "requestedPriority", "INVALID_PRIORITY"
        );

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("NORMAL"));
    }

    @Test
    @DisplayName("GET /api/queue/{equipmentId} - Should return empty queue for equipment with no bookings")
    void shouldReturnEmptyQueue() throws Exception {
        mockMvc.perform(get("/api/queue/{equipmentId}", ctId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private Booking createBooking(String patientName, Long equipmentId, Priority priority, String status) {
        Booking booking = new Booking();
        booking.setPatientName(patientName);
        booking.setEquipmentId(equipmentId);
        booking.setPriority(priority);
        booking.setSlotTime("2026-12-31T10:00");
        booking.setStatus(status);
        booking.setBookingTime(LocalDateTime.now());
        return bookingRepository.save(booking);
    }
}
