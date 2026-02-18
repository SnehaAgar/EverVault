package com.hospital.system.service;

import com.hospital.system.model.Booking;
import com.hospital.system.model.Priority;
import com.hospital.system.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hospital.system.model.Equipment;
import com.hospital.system.model.EquipmentStatus;
import com.hospital.system.repository.EquipmentRepository;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class QueueService {

    // Injected by Spring; these repositories use the DB connection from application.properties
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Booking createBookingRequest(Booking booking) {
        if (booking.getBookingTime() == null) {
            booking.setBookingTime(LocalDateTime.now());
        }

        // Check if equipment exists and is available
        Equipment equipment = equipmentRepository.findById(booking.getEquipmentId()).orElse(null);
        if (equipment == null) {
            throw new RuntimeException("Error: Equipment not found.");
        }
        if (equipment.getStatus() == EquipmentStatus.MAINTENANCE) {
            throw new RuntimeException("Error: " + equipment.getName() + " is under maintenance and cannot be booked.");
        }

        // Server-Side Date Validation (assuming slotTime is ISO string)
        if (booking.getSlotTime() != null && !booking.getSlotTime().isEmpty()) {
            try {
                LocalDateTime selected = LocalDateTime.parse(booking.getSlotTime());
                if (selected.isBefore(LocalDateTime.now())) {
                    throw new RuntimeException("Error: Booking date cannot be in the past.");
                }
            } catch (RuntimeException e) {
                // Re-throw our validation errors
                if (e.getMessage().contains("Booking date cannot be in the past")) {
                    throw e;
                }
                // For other parsing errors, continue with validation
            }
        }

        // Ensure status is PENDING (new bookings should always start as PENDING)
        booking.setStatus("PENDING");
        
        // If priority is not set, default to NORMAL
        if (booking.getPriority() == null) {
            booking.setPriority(Priority.NORMAL);
        }
        
        return bookingRepository.save(booking);
    }

    public List<Booking> getPendingBookings() {
        return bookingRepository.findByStatus("PENDING");
    }

    public Booking confirmBooking(Long id, Priority newPriority) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        booking.setPriority(newPriority);
        booking.setStatus("CONFIRMED");
        return bookingRepository.save(booking);
    }

    public List<Booking> getQueueForEquipment(Long equipmentId) {
        // ONLY get CONFIRMED bookings for the queue
        List<Booking> activeBookings = bookingRepository.findByEquipmentIdAndStatus(equipmentId, "CONFIRMED");
        
        // CORE LOGIC: Sort by Priority (High to Low), then Time (Oldest to Newest)
        return activeBookings.stream()
                .sorted(Comparator.comparing(Booking::getPriority).reversed()
                        .thenComparing(Booking::getBookingTime))
                .collect(Collectors.toList());
    }

    public Booking callNext(Long equipmentId) {
        List<Booking> queue = getQueueForEquipment(equipmentId);
        if (queue.isEmpty()) {
            return null;
        }
        Booking nextPatient = queue.get(0);
        
        // Update machine status
        Equipment eq = equipmentRepository.findById(equipmentId).orElse(null);
        if (eq != null) {
            eq.setStatus(EquipmentStatus.IN_USE);
            equipmentRepository.save(eq);
            
            // Schedule automatic status reversion after 5-10 seconds (using bufferTime as reference)
            // For demo purposes, use 8 seconds (between 5-10 seconds)
            int delaySeconds = 8;
            final Long bookingId = nextPatient.getId(); // Capture booking ID for closure
            scheduler.schedule(() -> {
                Equipment equipment = equipmentRepository.findById(equipmentId).orElse(null);
                if (equipment != null && equipment.getStatus() == EquipmentStatus.IN_USE) {
                    equipment.setStatus(EquipmentStatus.AVAILABLE);
                    equipmentRepository.save(equipment);
                    System.out.println("[EQUIPMENT] " + equipment.getName() + " is now AVAILABLE (auto-reverted after serving patient)");
                    
                    // Also mark the booking as SERVED
                    Booking booking = bookingRepository.findById(bookingId).orElse(null);
                    if (booking != null && booking.getStatus().equals("IN_USE")) {
                        booking.setStatus("SERVED");
                        bookingRepository.save(booking);
                        System.out.println("[BOOKING] Patient " + booking.getPatientName() + " has been served");
                    }
                }
            }, delaySeconds, TimeUnit.SECONDS);
        }

        // Mark booking as IN_USE
        nextPatient.setStatus("IN_USE");
        return bookingRepository.save(nextPatient);
    }

    public String calculateNextSlot(Long equipmentId) {
        Equipment eq = equipmentRepository.findById(equipmentId).orElse(null);
        if (eq == null) return "Unknown";
        if (eq.getStatus() == EquipmentStatus.MAINTENANCE) return "Under Repair";

        List<Booking> queue = getQueueForEquipment(equipmentId);
        if (queue.isEmpty()) return "Now";

        // Simple math: Now + (Queue Size * Machine Duration)
        LocalDateTime next = LocalDateTime.now().plusMinutes(queue.size() * (long) eq.getBufferTime());
        return next.toLocalTime().toString().substring(0, 5); // HH:mm format
    }

    public void markAsServed(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setStatus("SERVED");
        bookingRepository.save(booking);

        // Reset equipment status
        Equipment eq = equipmentRepository.findById(booking.getEquipmentId()).orElse(null);
        if (eq != null) {
            eq.setStatus(EquipmentStatus.AVAILABLE);
            equipmentRepository.save(eq);
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
