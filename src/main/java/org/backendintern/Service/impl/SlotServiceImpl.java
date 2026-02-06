package org.backendintern.Service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.backendintern.Mapper.SlotMapper;
import org.backendintern.Models.Entities.Doctor;
import org.backendintern.Models.Entities.Slot;
import org.backendintern.Models.Enums.TokenType;
import org.backendintern.Payloads.Request.CreateSlotRequest;
import org.backendintern.Payloads.Response.SlotResponse;
import org.backendintern.Repository.DoctorRepository;
import org.backendintern.Repository.SlotRepository;
import org.backendintern.Service.SlotServices;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SlotServiceImpl implements SlotServices {

        private final SlotRepository slotRepository;
        private final DoctorRepository doctorRepository;
        private final SlotMapper slotMapper;

        @Override
        public SlotResponse createSlot(CreateSlotRequest createSlotRequest) {
                Doctor doctor = doctorRepository.findById(createSlotRequest.getDoctorId())
                                .orElseThrow(() -> new RuntimeException("Doctor not found"));

                // check overlapping
                boolean overlap = slotRepository.existsOverLappingSlot(
                                doctor.getId(),
                                createSlotRequest.getStartTime(),
                                createSlotRequest.getEndTime());

                if (overlap) {
                        throw new RuntimeException("Overlapping slot exists for this doctor");
                }

                Slot slot = Slot.builder()
                                .doctor(doctor)
                                .startTime(createSlotRequest.getStartTime())
                                .endTime(createSlotRequest.getEndTime())
                                .maxCapacity(
                                                createSlotRequest.getMaxCapacity() != 0
                                                                ? createSlotRequest.getMaxCapacity()
                                                                : 20)
                                .currentCapacity(0)
                                .build();
                Slot saved = slotRepository.save(slot);

                return slotMapper.toResponse(saved);
        }

        @Override
        public SlotResponse getSlotById(UUID slotId) {
                Slot slot = slotRepository.findById(slotId)
                                .orElseThrow(() -> new RuntimeException("Slot not found"));
                return slotMapper.toResponse(slot);

        }

        @Override
        public List<SlotResponse> getAvailableSlots(UUID doctorId) {
                return slotRepository
                                .findAvailableSlotsByDoctor(doctorId)
                                .stream()
                                .map(slotMapper::toResponse)
                                .toList();
        }

        @Override
        public void closeSlot(UUID slotId) {
                Slot slot = slotRepository.findById(slotId)
                                .orElseThrow(() -> new RuntimeException("Slot not found"));
                slot.setEndTime(LocalDateTime.now());
        }

        @Override
        public SlotResponse findBestEmergencySlot(LocalDateTime currentTime) {
                List<Slot> availableSlots = slotRepository.findAvailableSlotsByCurrentTime(currentTime);
                 return availableSlots.stream()
                                .filter(slot -> !slotHasEmergency(slot)) // skip slot already handling emergency
                                .sorted(Comparator.comparing(Slot::getStartTime)) // earliest slot first
                                .findFirst()
                              .map(slotMapper::toResponse)
                                .orElseGet(() -> {
                                        //FallBack -> if all slots have emergencies or are full,
                                        // just pick the earliest one , Dont throw Exception
                                        return availableSlots.stream()
                                                .sorted(Comparator.comparing(Slot::getStartTime))
                                                .findFirst()
                                                .map(slotMapper::toResponse)
                                                .orElseThrow(() -> new RuntimeException("Clinic is closed (No slots exist)"));
                                });

        }

        private boolean slotHasEmergency(Slot slot) {
                return slot.getTokens().stream()
                                .anyMatch(t -> t.getTokenType() == TokenType.EMERGENCY
                                                && t.getTokenStatus() != null
                                                && t.getTokenStatus().name().equals("ACTIVE"));
        }

}
