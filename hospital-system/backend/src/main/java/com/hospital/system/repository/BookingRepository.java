package com.hospital.system.repository;

import com.hospital.system.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for the Booking entity. Spring Data JPA implements this interface and
 * runs queries against the "booking" table using the configured DataSource.
 * Custom method names (e.g. findByStatus) are turned into SQL by Spring (e.g. WHERE status = ?).
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByEquipmentId(Long equipmentId);
    List<Booking> findByStatus(String status);
    List<Booking> findByEquipmentIdAndStatus(Long equipmentId, String status);
}
