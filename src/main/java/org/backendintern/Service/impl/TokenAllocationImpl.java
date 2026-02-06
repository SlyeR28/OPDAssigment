package org.backendintern.Service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.backendintern.Mapper.TokenMapper;
import org.backendintern.Models.Entities.Slot;
import org.backendintern.Models.Entities.Token;
import org.backendintern.Models.Enums.TokenStatus;
import org.backendintern.Models.Enums.TokenType;
import org.backendintern.Payloads.Request.CreateTokenRequest;
import org.backendintern.Payloads.Response.TokenResponse;
import org.backendintern.Repository.SlotRepository;
import org.backendintern.Repository.TokenRepository;
import org.backendintern.Service.SlotServices;
import org.backendintern.Service.TokenAllocationServices;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenAllocationImpl implements TokenAllocationServices {

    private final SlotRepository slotRepository;
    private final TokenRepository tokenRepository;
    private final SlotServices slotServices;
    private final TokenMapper tokenMapper;

    private final ConcurrentHashMap<UUID, PriorityBlockingQueue<CreateTokenRequest>> slotWaitlists
            = new ConcurrentHashMap<>();

    @Override
    public TokenResponse allocateToken(CreateTokenRequest createTokenRequest) {
        Slot slot;

        if (createTokenRequest.getTokenType() == TokenType.EMERGENCY) {
            // EMERGENCY: find best available slot ignoring order
            slot = slotServices.findBestEmergencySlot(LocalDateTime.now())
                    .getSlotId() != null
                            ? slotRepository.findById(slotServices.findBestEmergencySlot(LocalDateTime.now())
                                    .getSlotId()).orElseThrow()
                            : null;

        } else {
            // Normal token: find doctor available slot
            UUID doctorId = createTokenRequest.getDoctorId();
            slot = slotRepository.findAvailableSlotsByDoctor(doctorId)
                    .stream()
                    .filter(s -> s.canBook(createTokenRequest.getTokenType()))
                    .findFirst().orElse(null);
        }
        // slot full -> waitlist
        if (slot == null || !slot.canBook(createTokenRequest.getTokenType())) {
            addToWaitlist(createTokenRequest,
                   slot != null ? slot.getId() : createTokenRequest.getDoctorId());
            return TokenResponse.builder()
                    .patientName(createTokenRequest.getPatientName())
                    .tokenType(createTokenRequest.getTokenType())
                    .tokenStatus(TokenStatus.WAITLISTED)
                    .score(createTokenRequest.getTokenType().getScore())
                    .build();

        }
        return saveTokenToSlot(slot, createTokenRequest);

    }

    @Override
    public void cancelToken(UUID tokenId) {
        Token tokenNotFound = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        tokenNotFound.setTokenStatus(TokenStatus.CANCELLED);

        Slot slot = tokenNotFound.getTimeSlot();

        if (slot.getCurrentCapacity() > 0) {
            slot.setCurrentCapacity(slot.getCurrentCapacity() - 1);
            slotRepository.save(slot);

            // Allocate next token from waitlist if any
            allocateFromWaitlist(slot);
        }
        tokenRepository.save(tokenNotFound);
    }

    @Override
    public List<TokenResponse> getAllTokensforDoctors(UUID doctorId) {
        return tokenRepository.findAllByTimeSlotDoctorId(doctorId)
                .stream()
                .map(tokenMapper::toResponse)
                .toList();
    }

    @Override
    public List<TokenResponse> getTokensBySlot(UUID slotId) {
        return tokenRepository.findByTimeSlotId(slotId)
                .stream()
                .map(tokenMapper::toResponse)
                .toList();
    }

    @Override
    public void handleNoShowToken(UUID tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        token.setTokenStatus(TokenStatus.NO_SHOW);
        tokenRepository.save(token);

        Slot slot = token.getTimeSlot();
        if (slot.getCurrentCapacity() > 0) {
            slot.setCurrentCapacity(slot.getCurrentCapacity() - 1);
            slotRepository.save(slot);

            allocateFromWaitlist(slot);
        }

    }

    // helper method
    private void addToWaitlist(CreateTokenRequest createTokenRequest, UUID slotId) {
        // use DoctorId as key for waiting queue
        slotWaitlists.computeIfAbsent(slotId,
                        k -> new PriorityBlockingQueue<>(
                20,
                (t1, t2) -> Integer.compare(t2.getTokenType().getScore(), t1.getTokenType().getScore())))
                .offer(createTokenRequest);
    }

    private void allocateFromWaitlist(Slot slot) {

        PriorityBlockingQueue<CreateTokenRequest> queue = slotWaitlists.get(slot.getId());
        if (queue == null) return;

        while (!queue.isEmpty() && slot.canBook(queue.peek().getTokenType())) {
            CreateTokenRequest request = queue.poll();
            if (request != null) {
                saveTokenToSlot(slot, request);
            }
        }
    }

    private TokenResponse saveTokenToSlot(Slot slot, CreateTokenRequest createTokenRequest) {

        // elastic capacity for emergency
        if(createTokenRequest.getTokenType() == TokenType.EMERGENCY) {
            slot.setElasticCapacity(slot.getElasticCapacity() + 1);
        }


        slot.incrementCapacity(createTokenRequest.getTokenType());
        slotRepository.save(slot);

        Token build = Token.builder()
                .patientName(createTokenRequest.getPatientName())
                .tokenType(createTokenRequest.getTokenType())
                .tokenStatus(TokenStatus.ACTIVE)
                .timeSlot(slot)
                .score(createTokenRequest.getTokenType().getScore())
                .build();
        Token save = tokenRepository.save(build);
        return tokenMapper.toResponse(save);
    }
}
