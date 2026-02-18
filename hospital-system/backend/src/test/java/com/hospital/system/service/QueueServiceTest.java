package com.hospital.system.service;

import com.hospital.system.model.Booking;
import com.hospital.system.model.Priority;
import com.hospital.system.model.Equipment;
import com.hospital.system.model.EquipmentStatus;
import com.hospital.system.repository.BookingRepository;
import com.hospital.system.repository.EquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QueueServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private QueueService queueService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPrioritySorting() {
        Long mriId = 1L;
        List<Booking> mockBookings = new ArrayList<>();
        
        // Normal patient who arrived first
        Booking normal = new Booking(1L, "Normal Patient", mriId, Priority.NORMAL, "2026-02-14T10:00", "CONFIRMED", LocalDateTime.now().minusMinutes(30));
        // Emergency patient who arrived later
        Booking emergency = new Booking(2L, "Emergency Patient", mriId, Priority.EMERGENCY, "2026-02-14T10:05", "CONFIRMED", LocalDateTime.now().minusMinutes(10));
        
        mockBookings.add(normal);
        mockBookings.add(emergency);

        when(bookingRepository.findByEquipmentIdAndStatus(mriId, "CONFIRMED")).thenReturn(mockBookings);

        List<Booking> sortedQueue = queueService.getQueueForEquipment(mriId);

        assertEquals(2, sortedQueue.size());
        assertEquals("Emergency Patient", sortedQueue.get(0).getPatientName()); // Priority 1st
        assertEquals("Normal Patient", sortedQueue.get(1).getPatientName());    // Normal 2nd
    }

    @Test
    void testMarkAsServed() {
        Long bookingId = 10L;
        Long mriId = 1L;
        Booking booking = new Booking(bookingId, "John Doe", mriId, Priority.NORMAL, "2026-02-14T10:00", "IN_USE", LocalDateTime.now());
        Equipment mri = new Equipment(mriId, "MRI-1", "MRI", EquipmentStatus.IN_USE, 60);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(equipmentRepository.findById(mriId)).thenReturn(Optional.of(mri));

        queueService.markAsServed(bookingId);

        assertEquals("SERVED", booking.getStatus());
        assertEquals(EquipmentStatus.AVAILABLE, mri.getStatus());
    }

    @Test
    void testCalculateNextSlotEmpty() {
        Long mriId = 1L;
        Equipment mri = new Equipment(mriId, "MRI-1", "MRI", EquipmentStatus.AVAILABLE, 60);
        
        when(equipmentRepository.findById(mriId)).thenReturn(Optional.of(mri));
        when(bookingRepository.findByEquipmentIdAndStatus(mriId, "CONFIRMED")).thenReturn(new ArrayList<>());

        String next = queueService.calculateNextSlot(mriId);
        assertEquals("Now", next);
    }

    @Test
    void testCreateBookingRequestDefaults() {
        Long equipmentId = 1L;
        Equipment equipment = new Equipment(equipmentId, "MRI-1", "MRI", EquipmentStatus.AVAILABLE, 60);
        
        Booking booking = new Booking();
        booking.setPatientName("Test Patient");
        booking.setEquipmentId(equipmentId);
        booking.setSlotTime("2026-12-31T10:00");
        // Not setting priority or status - should default

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Booking result = queueService.createBookingRequest(booking);

        assertNotNull(result);
        assertEquals(Priority.NORMAL, result.getPriority());
        assertEquals("PENDING", result.getStatus());
        assertNotNull(result.getBookingTime());
    }

    @Test
    void testGetPendingBookings() {
        List<Booking> pendingList = new ArrayList<>();
        pendingList.add(new Booking(1L, "Pending 1", 1L, Priority.NORMAL, "2026-02-14T10:00", "PENDING", LocalDateTime.now()));
        pendingList.add(new Booking(2L, "Pending 2", 1L, Priority.EMERGENCY, "2026-02-14T11:00", "PENDING", LocalDateTime.now()));

        when(bookingRepository.findByStatus("PENDING")).thenReturn(pendingList);

        List<Booking> result = queueService.getPendingBookings();

        assertEquals(2, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
        assertEquals("PENDING", result.get(1).getStatus());
    }

    @Test
    void testConfirmBooking() {
        Long bookingId = 5L;
        Booking pending = new Booking(bookingId, "To Confirm", 1L, Priority.NORMAL, "2026-02-14T10:00", "PENDING", LocalDateTime.now());

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(pending));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = queueService.confirmBooking(bookingId, Priority.EMERGENCY);

        assertEquals("CONFIRMED", result.getStatus());
        assertEquals(Priority.EMERGENCY, result.getPriority());
    }

    @Test
    void testCallNextWithEmptyQueue() {
        Long mriId = 1L;
        when(bookingRepository.findByEquipmentIdAndStatus(mriId, "CONFIRMED")).thenReturn(new ArrayList<>());

        Booking result = queueService.callNext(mriId);

        assertNull(result);
    }

    @Test
    void testCalculateNextSlotMaintenance() {
        Long mriId = 1L;
        Equipment mri = new Equipment(mriId, "MRI-1", "MRI", EquipmentStatus.MAINTENANCE, 60);

        when(equipmentRepository.findById(mriId)).thenReturn(Optional.of(mri));

        String next = queueService.calculateNextSlot(mriId);
        assertEquals("Under Repair", next);
    }

    @Test
    void testCalculateNextSlotWithQueue() {
        Long mriId = 1L;
        Equipment mri = new Equipment(mriId, "MRI-1", "MRI", EquipmentStatus.AVAILABLE, 60);
        
        List<Booking> queue = new ArrayList<>();
        queue.add(new Booking(1L, "Patient 1", mriId, Priority.NORMAL, "2026-02-14T10:00", "CONFIRMED", LocalDateTime.now()));
        queue.add(new Booking(2L, "Patient 2", mriId, Priority.NORMAL, "2026-02-14T11:00", "CONFIRMED", LocalDateTime.now()));

        when(equipmentRepository.findById(mriId)).thenReturn(Optional.of(mri));
        when(bookingRepository.findByEquipmentIdAndStatus(mriId, "CONFIRMED")).thenReturn(queue);

        String next = queueService.calculateNextSlot(mriId);
        
        // Should return a time string in HH:mm format
        assertNotNull(next);
        assertNotEquals("Now", next);
        assertNotEquals("Under Repair", next);
        assertTrue(next.contains(":"));
    }

    @Test
    void testMarkAsServedBookingNotFound() {
        Long nonExistentId = 999L;
        when(bookingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> queueService.markAsServed(nonExistentId));
    }

    @Test
    void testSamePrioritySortingByTime() {
        Long mriId = 1L;
        List<Booking> mockBookings = new ArrayList<>();
        
        // Both NORMAL priority, but arrived at different times
        Booking later = new Booking(1L, "Later Patient", mriId, Priority.NORMAL, "2026-02-14T10:00", "CONFIRMED", LocalDateTime.now().minusMinutes(10));
        Booking earlier = new Booking(2L, "Earlier Patient", mriId, Priority.NORMAL, "2026-02-14T10:05", "CONFIRMED", LocalDateTime.now().minusMinutes(30));
        
        mockBookings.add(later);
        mockBookings.add(earlier);

        when(bookingRepository.findByEquipmentIdAndStatus(mriId, "CONFIRMED")).thenReturn(mockBookings);

        List<Booking> sortedQueue = queueService.getQueueForEquipment(mriId);

        assertEquals(2, sortedQueue.size());
        assertEquals("Earlier Patient", sortedQueue.get(0).getPatientName()); // Earlier time first
        assertEquals("Later Patient", sortedQueue.get(1).getPatientName());
    }

    @Test
    void testCreateBookingForMaintenanceEquipment() {
        Long ventilatorId = 3L;
        Equipment ventilator = new Equipment(ventilatorId, "Ventilator-1", "Ventilator", EquipmentStatus.MAINTENANCE, 1440);
        
        when(equipmentRepository.findById(ventilatorId)).thenReturn(Optional.of(ventilator));

        Booking booking = new Booking();
        booking.setPatientName("Test Patient");
        booking.setEquipmentId(ventilatorId);
        booking.setSlotTime("2026-12-31T10:00");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            queueService.createBookingRequest(booking);
        });

        assertTrue(exception.getMessage().contains("under maintenance"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBookingForNonExistentEquipment() {
        Long nonExistentId = 999L;
        
        when(equipmentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Booking booking = new Booking();
        booking.setPatientName("Test Patient");
        booking.setEquipmentId(nonExistentId);
        booking.setSlotTime("2026-12-31T10:00");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            queueService.createBookingRequest(booking);
        });

        assertTrue(exception.getMessage().contains("Equipment not found"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBookingWithPastDate() {
        Long equipmentId = 1L;
        Equipment equipment = new Equipment(equipmentId, "MRI-1", "MRI", EquipmentStatus.AVAILABLE, 60);
        
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));

        Booking booking = new Booking();
        booking.setPatientName("Test Patient");
        booking.setEquipmentId(equipmentId);
        booking.setSlotTime("2020-02-15T10:00"); // Past date

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            queueService.createBookingRequest(booking);
        });

        assertTrue(exception.getMessage().contains("Booking date cannot be in the past"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}
