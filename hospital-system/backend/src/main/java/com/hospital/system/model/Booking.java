package com.hospital.system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity mapped to the "booking" table in the database.
 * JPA uses the same DataSource (from application.properties) to persist bookings.
 */
@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientName;
    
    private Long equipmentId; // Initial simplification: one queue per equipment

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private String slotTime; // e.g., "2026-02-14T10:30"
    private String status;   // PENDING, CONFIRMED

    private LocalDateTime bookingTime;

    // No-args constructor
    public Booking() {
    }

    // Constructor for initialization
    public Booking(Long id, String patientName, Long equipmentId, Priority priority, String slotTime, String status, LocalDateTime bookingTime) {
        this.id = id;
        this.patientName = patientName;
        this.equipmentId = equipmentId;
        this.priority = priority;
        this.slotTime = slotTime;
        this.status = status;
        this.bookingTime = bookingTime;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getSlotTime() {
        return slotTime;
    }

    public void setSlotTime(String slotTime) {
        this.slotTime = slotTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }
}
