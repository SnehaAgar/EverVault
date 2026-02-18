package com.hospital.system.repository;

import com.hospital.system.model.Booking;
import com.hospital.system.model.Priority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for Booking entity.
 * Uses @DataJpaTest with in-memory H2 database.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Should save and retrieve a booking by ID")
    void testSaveAndFindById() {
        // Given
        Booking booking = new Booking();
        booking.setPatientName("John Doe");
        booking.setEquipmentId(1L);
        booking.setPriority(Priority.NORMAL);
        booking.setSlotTime("2026-02-14T10:00");
        booking.setStatus("PENDING");
        booking.setBookingTime(LocalDateTime.now());

        // When
        Booking saved = bookingRepository.save(booking);
        Booking found = bookingRepository.findById(saved.getId()).orElse(null);

        // Then
        assertNotNull(found);
        assertEquals("John Doe", found.getPatientName());
        assertEquals(1L, found.getEquipmentId());
        assertEquals(Priority.NORMAL, found.getPriority());
        assertEquals("PENDING", found.getStatus());
    }

    @Test
    @DisplayName("Should find bookings by equipment ID")
    void testFindByEquipmentId() {
        // Given
        Booking booking1 = createBooking("Patient 1", 1L, Priority.NORMAL, "PENDING");
        Booking booking2 = createBooking("Patient 2", 1L, Priority.EMERGENCY, "CONFIRMED");
        Booking booking3 = createBooking("Patient 3", 2L, Priority.NORMAL, "PENDING");

        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);
        entityManager.flush();

        // When
        List<Booking> equipment1Bookings = bookingRepository.findByEquipmentId(1L);

        // Then
        assertEquals(2, equipment1Bookings.size());
        assertTrue(equipment1Bookings.stream().anyMatch(b -> b.getPatientName().equals("Patient 1")));
        assertTrue(equipment1Bookings.stream().anyMatch(b -> b.getPatientName().equals("Patient 2")));
    }

    @Test
    @DisplayName("Should find bookings by status")
    void testFindByStatus() {
        // Given
        Booking pending1 = createBooking("Pending Patient 1", 1L, Priority.NORMAL, "PENDING");
        Booking pending2 = createBooking("Pending Patient 2", 2L, Priority.EMERGENCY, "PENDING");
        Booking confirmed = createBooking("Confirmed Patient", 1L, Priority.NORMAL, "CONFIRMED");

        entityManager.persist(pending1);
        entityManager.persist(pending2);
        entityManager.persist(confirmed);
        entityManager.flush();

        // When
        List<Booking> pendingBookings = bookingRepository.findByStatus("PENDING");

        // Then
        assertEquals(2, pendingBookings.size());
        assertTrue(pendingBookings.stream().allMatch(b -> b.getStatus().equals("PENDING")));
    }

    @Test
    @DisplayName("Should find bookings by equipment ID and status")
    void testFindByEquipmentIdAndStatus() {
        // Given
        Booking mriPending = createBooking("MRI Pending", 1L, Priority.NORMAL, "PENDING");
        Booking mriConfirmed = createBooking("MRI Confirmed", 1L, Priority.NORMAL, "CONFIRMED");
        Booking ctPending = createBooking("CT Pending", 2L, Priority.NORMAL, "PENDING");

        entityManager.persist(mriPending);
        entityManager.persist(mriConfirmed);
        entityManager.persist(ctPending);
        entityManager.flush();

        // When
        List<Booking> mriConfirmedBookings = bookingRepository.findByEquipmentIdAndStatus(1L, "CONFIRMED");

        // Then
        assertEquals(1, mriConfirmedBookings.size());
        assertEquals("MRI Confirmed", mriConfirmedBookings.get(0).getPatientName());
    }

    @Test
    @DisplayName("Should delete a booking")
    void testDeleteBooking() {
        // Given
        Booking booking = createBooking("To Delete", 1L, Priority.NORMAL, "PENDING");
        Booking saved = entityManager.persist(booking);
        entityManager.flush();

        // When
        bookingRepository.deleteById(saved.getId());

        // Then
        assertFalse(bookingRepository.findById(saved.getId()).isPresent());
    }

    @Test
    @DisplayName("Should update booking status")
    void testUpdateBookingStatus() {
        // Given
        Booking booking = createBooking("Update Test", 1L, Priority.NORMAL, "PENDING");
        Booking saved = entityManager.persist(booking);
        entityManager.flush();

        // When
        saved.setStatus("CONFIRMED");
        saved.setPriority(Priority.EMERGENCY);
        Booking updated = bookingRepository.save(saved);

        // Then
        assertEquals("CONFIRMED", updated.getStatus());
        assertEquals(Priority.EMERGENCY, updated.getPriority());
    }

    private Booking createBooking(String patientName, Long equipmentId, Priority priority, String status) {
        Booking booking = new Booking();
        booking.setPatientName(patientName);
        booking.setEquipmentId(equipmentId);
        booking.setPriority(priority);
        booking.setSlotTime("2026-02-14T10:00");
        booking.setStatus(status);
        booking.setBookingTime(LocalDateTime.now());
        return booking;
    }
}
