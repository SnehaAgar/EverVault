package com.hospital.system.repository;

import com.hospital.system.model.Equipment;
import com.hospital.system.model.EquipmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for Equipment entity.
 * Uses @DataJpaTest with in-memory H2 database.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class EquipmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Test
    @DisplayName("Should save and retrieve equipment by ID")
    void testSaveAndFindById() {
        // Given
        Equipment mri = new Equipment();
        mri.setName("MRI-1");
        mri.setType("MRI");
        mri.setStatus(EquipmentStatus.AVAILABLE);
        mri.setBufferTime(60);

        // When
        Equipment saved = equipmentRepository.save(mri);
        Optional<Equipment> found = equipmentRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("MRI-1", found.get().getName());
        assertEquals("MRI", found.get().getType());
        assertEquals(EquipmentStatus.AVAILABLE, found.get().getStatus());
        assertEquals(60, found.get().getBufferTime());
    }

    @Test
    @DisplayName("Should find all equipment")
    void testFindAll() {
        // Given
        Equipment mri = createEquipment("MRI-1", "MRI", EquipmentStatus.AVAILABLE, 60);
        Equipment ct = createEquipment("CT-1", "CT", EquipmentStatus.AVAILABLE, 30);
        Equipment xray = createEquipment("X-Ray-1", "X-Ray", EquipmentStatus.MAINTENANCE, 15);

        entityManager.persist(mri);
        entityManager.persist(ct);
        entityManager.persist(xray);
        entityManager.flush();

        // When
        List<Equipment> allEquipment = equipmentRepository.findAll();

        // Then
        assertEquals(3, allEquipment.size());
    }

    @Test
    @DisplayName("Should update equipment status")
    void testUpdateStatus() {
        // Given
        Equipment mri = createEquipment("MRI-1", "MRI", EquipmentStatus.AVAILABLE, 60);
        Equipment saved = entityManager.persist(mri);
        entityManager.flush();

        // When
        saved.setStatus(EquipmentStatus.IN_USE);
        Equipment updated = equipmentRepository.save(saved);

        // Then
        assertEquals(EquipmentStatus.IN_USE, updated.getStatus());
    }

    @Test
    @DisplayName("Should delete equipment")
    void testDeleteEquipment() {
        // Given
        Equipment ct = createEquipment("CT-1", "CT", EquipmentStatus.AVAILABLE, 30);
        Equipment saved = entityManager.persist(ct);
        entityManager.flush();

        // When
        equipmentRepository.deleteById(saved.getId());

        // Then
        assertFalse(equipmentRepository.findById(saved.getId()).isPresent());
    }

    @Test
    @DisplayName("Should find equipment by different statuses")
    void testFindByStatus() {
        // Given
        Equipment availableMri = createEquipment("MRI-1", "MRI", EquipmentStatus.AVAILABLE, 60);
        Equipment inUseCt = createEquipment("CT-1", "CT", EquipmentStatus.IN_USE, 30);
        Equipment maintenanceXray = createEquipment("X-Ray-1", "X-Ray", EquipmentStatus.MAINTENANCE, 15);

        entityManager.persist(availableMri);
        entityManager.persist(inUseCt);
        entityManager.persist(maintenanceXray);
        entityManager.flush();

        // When & Then
        List<Equipment> allEquipment = equipmentRepository.findAll();
        long availableCount = allEquipment.stream()
                .filter(e -> e.getStatus() == EquipmentStatus.AVAILABLE)
                .count();
        long inUseCount = allEquipment.stream()
                .filter(e -> e.getStatus() == EquipmentStatus.IN_USE)
                .count();

        assertEquals(1, availableCount);
        assertEquals(1, inUseCount);
    }

    @Test
    @DisplayName("Should handle equipment with same name but different IDs")
    void testMultipleEquipmentSameName() {
        // Given
        Equipment mri1 = createEquipment("MRI-1", "MRI", EquipmentStatus.AVAILABLE, 60);
        Equipment mri2 = createEquipment("MRI-1", "MRI", EquipmentStatus.AVAILABLE, 60);

        entityManager.persist(mri1);
        entityManager.persist(mri2);
        entityManager.flush();

        // When
        List<Equipment> allEquipment = equipmentRepository.findAll();

        // Then
        assertEquals(2, allEquipment.size());
        assertNotEquals(mri1.getId(), mri2.getId());
    }

    private Equipment createEquipment(String name, String type, EquipmentStatus status, int bufferTime) {
        Equipment equipment = new Equipment();
        equipment.setName(name);
        equipment.setType(type);
        equipment.setStatus(status);
        equipment.setBufferTime(bufferTime);
        return equipment;
    }
}
