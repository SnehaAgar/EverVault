package com.hospital.system.repository;

import com.hospital.system.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for the Equipment entity. Spring Data JPA implements this interface automatically
 * and uses the database connection (from application.properties) to run SQL.
 * Methods like findAll(), findById(), save() are provided by JpaRepository.
 */
@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
}
