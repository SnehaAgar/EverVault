package com.hospital.system.model;

import jakarta.persistence.*;

/**
 * Entity mapped to the "equipment" table in the database.
 * JPA/Hibernate uses the connection from application.properties to read/write this table.
 */
@Entity
@Table(name = "equipment")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // e.g., MRI-1
    private String type; // e.g., MRI

    @Enumerated(EnumType.STRING)
    private EquipmentStatus status;

    private int bufferTime; // in minutes (Procedure Duration)

    @Transient  // not stored in DB; computed at runtime
    private String nextAvailable;

    @Transient  // not stored in DB; computed at runtime
    private int queueLength;

    // No-args constructor
    public Equipment() {
    }

    // Custom constructor for DataInitializer
    public Equipment(Long id, String name, String type, EquipmentStatus status, int bufferTime) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.bufferTime = bufferTime;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }

    public int getBufferTime() {
        return bufferTime;
    }

    public void setBufferTime(int bufferTime) {
        this.bufferTime = bufferTime;
    }

    public String getNextAvailable() {
        return nextAvailable;
    }

    public void setNextAvailable(String nextAvailable) {
        this.nextAvailable = nextAvailable;
    }

    public int getQueueLength() {
        return queueLength;
    }

    public void setQueueLength(int queueLength) {
        this.queueLength = queueLength;
    }
}
