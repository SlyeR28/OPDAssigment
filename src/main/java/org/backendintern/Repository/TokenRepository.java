package org.backendintern.Repository;

import org.backendintern.Models.Entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {

    List<Token> findByTimeSlotId(UUID slotId);

    List<Token> findAllByTimeSlotDoctorId(UUID doctorId);


}