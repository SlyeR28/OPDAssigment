package org.backendintern.Repository;

import org.backendintern.Models.Entities.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SlotRepository extends JpaRepository<Slot, UUID> {

        // find all slots by doctor id
        List<Slot> findByDoctorId(UUID doctorId);

        // find slot for a doctor that are not full
        @Query("SELECT s FROM Slot s WHERE s.doctor.id = :doctorId AND s.currentCapacity < s.maxCapacity")
        List<Slot> findAvailableSlotsByDoctor(@Param("doctorId") UUID doctorId);

        // checking if a new slot overlap
        @Query("""
                        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
                        FROM Slot s
                        WHERE s.doctor.id = :doctorId
                        AND s.startTime < :endTime
                        AND s.endTime > :startTime

                        """)
        boolean existsOverLappingSlot(
                        @Param("doctorId") UUID doctorId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        // Find any available slots for emergency patients (ignore doctor)
        @Query("""
                          SELECT s FROM Slot s
                                  WHERE s.currentCapacity < (s.maxCapacity + s.elasticCapacity)
                                   AND s.endTime >= :currentTime
                        """)
        List<Slot> findAvailableSlotsByCurrentTime(@Param("currentTime") LocalDateTime currentTime);

}