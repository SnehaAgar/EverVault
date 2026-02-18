package com.hospital.system.controller;

import com.hospital.system.model.Booking;
import com.hospital.system.model.Equipment;
import com.hospital.system.model.Priority;
import com.hospital.system.repository.EquipmentRepository;
import com.hospital.system.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Modernize to allow all for demo, or keep specific if preferred
@Tag(name = "Health Logistics", description = "EverVault API for managing hospital equipment and patient triage")
public class HospitalController {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private QueueService queueService;

    @Operation(summary = "Fetch all health facilities", description = "Returns active MRI, CT, and specialized equipment status")
    @GetMapping("/equipment")
    public List<Equipment> getAllEquipment() {
        List<Equipment> equipmentList = equipmentRepository.findAll();
        for (Equipment eq : equipmentList) {
            List<Booking> queue = queueService.getQueueForEquipment(eq.getId());
            eq.setQueueLength(queue.size());
            eq.setNextAvailable(queueService.calculateNextSlot(eq.getId()));
        }
        return equipmentList;
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleRuntimeException(RuntimeException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return error;
    }

    @Operation(summary = "New patient booking", description = "Creates a triage request for a specific machine")
    @PostMapping("/bookings")
    public Booking createBooking(@RequestBody Map<String, Object> payload) {
        Booking booking = new Booking();
        booking.setPatientName((String) payload.get("patientName"));
        booking.setEquipmentId(Long.valueOf(payload.get("equipmentId").toString()));
        booking.setSlotTime((String) payload.get("slotTime"));
        
        // Map requestedPriority to priority enum
        String requestedPriority = (String) payload.get("requestedPriority");
        if (requestedPriority != null) {
            try {
                booking.setPriority(Priority.valueOf(requestedPriority));
            } catch (IllegalArgumentException e) {
                booking.setPriority(Priority.NORMAL); // Default to NORMAL if invalid
            }
        }
        
        return queueService.createBookingRequest(booking);
    }

    @Operation(summary = "List triage requests", description = "Fetch patients waiting for admin priority assignment")
    @GetMapping("/bookings/pending")
    public List<Booking> getPending() {
        return queueService.getPendingBookings();
    }

    @Operation(summary = "Confirm triage", description = "Assigns priority and moves patient to the live operational queue")
    @PostMapping("/bookings/{id}/confirm")
    public Booking confirmBooking(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Priority priority = Priority.valueOf(payload.get("assignedPriority"));
        return queueService.confirmBooking(id, priority);
    }

    @Operation(summary = "Operational live queue", description = "Get sorted patient list for a specific machine")
    @GetMapping("/queue/{equipmentId}")
    public List<Booking> getQueue(@PathVariable Long equipmentId) {
        return queueService.getQueueForEquipment(equipmentId);
    }

    @Operation(summary = "Commence procedure", description = "Calls the next patient and marks equipment as IN_USE")
    @PostMapping("/queue/{equipmentId}/next")
    public Booking callNext(@PathVariable Long equipmentId) {
        return queueService.callNext(equipmentId);
    }

    @Operation(summary = "Patient served", description = "Ends procedure and reverts machine to AVAILABLE")
    @PostMapping("/bookings/{id}/serve")
    public ResponseEntity<Void> servePatient(@PathVariable Long id) {
        queueService.markAsServed(id);
        return ResponseEntity.ok().build();
    }
}
