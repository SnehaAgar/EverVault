package com.hospital.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.system.model.Booking;
import com.hospital.system.model.Equipment;
import com.hospital.system.model.EquipmentStatus;
import com.hospital.system.model.Priority;
import com.hospital.system.repository.EquipmentRepository;
import com.hospital.system.service.QueueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lightweight controller tests for {@link HospitalController}.
 * Uses MockMvc + mocked service/repository (no real DB).
 */
@WebMvcTest(HospitalController.class)
class HospitalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EquipmentRepository equipmentRepository;

    @MockBean
    private QueueService queueService;

    @Test
    @DisplayName("GET /api/equipment returns enriched equipment list")
    void shouldReturnEquipmentWithQueueInfo() throws Exception {
        Equipment mri = new Equipment(1L, "MRI-1", "MRI", EquipmentStatus.AVAILABLE, 60);
        Equipment ct = new Equipment(2L, "CT-Scanner", "CT", EquipmentStatus.AVAILABLE, 30);

        when(equipmentRepository.findAll()).thenReturn(List.of(mri, ct));
        // For MRI, pretend there is 1 booking in queue
        when(queueService.getQueueForEquipment(1L))
                .thenReturn(List.of(buildBooking(1L, 1L, Priority.NORMAL, "CONFIRMED")));
        when(queueService.calculateNextSlot(1L)).thenReturn("10:30");

        // For CT, queue is empty
        when(queueService.getQueueForEquipment(2L)).thenReturn(List.of());
        when(queueService.calculateNextSlot(2L)).thenReturn("Now");

        mockMvc.perform(get("/api/equipment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("MRI-1"))
                .andExpect(jsonPath("$[0].queueLength").value(1))
                .andExpect(jsonPath("$[0].nextAvailable").value("10:30"))
                .andExpect(jsonPath("$[1].name").value("CT-Scanner"))
                .andExpect(jsonPath("$[1].queueLength").value(0))
                .andExpect(jsonPath("$[1].nextAvailable").value("Now"));
    }

    @Test
    @DisplayName("POST /api/bookings creates booking via QueueService")
    void shouldCreateBooking() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "patientName", "API Test",
                "equipmentId", 1,
                "slotTime", "2026-02-14T10:00",
                "requestedPriority", "EMERGENCY"
        );

        Booking saved = new Booking(
                99L,
                "API Test",
                1L,
                Priority.EMERGENCY,
                "2026-02-14T10:00",
                "PENDING",
                LocalDateTime.now()
        );

        when(queueService.createBookingRequest(any(Booking.class))).thenReturn(saved);

        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.patientName").value("API Test"))
                .andExpect(jsonPath("$.equipmentId").value(1))
                .andExpect(jsonPath("$.priority").value("EMERGENCY"));

        // optional: verify that QueueService was called with mapped values
        Mockito.verify(queueService).createBookingRequest(any(Booking.class));
    }

    private Booking buildBooking(Long id, Long equipmentId, Priority priority, String status) {
        return new Booking(
                id,
                "Test Patient " + id,
                equipmentId,
                priority,
                "2026-02-14T10:00",
                status,
                LocalDateTime.now()
        );
    }
}

