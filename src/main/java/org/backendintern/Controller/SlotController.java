package org.backendintern.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.backendintern.Payloads.Request.CreateSlotRequest;
import org.backendintern.Payloads.Response.SlotResponse;
import org.backendintern.Service.SlotServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotServices slotServices;

    @PostMapping("/add")
    public ResponseEntity<SlotResponse> createSlot(@Valid @RequestBody CreateSlotRequest request) {
        return ResponseEntity.ok(slotServices.createSlot(request));
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<SlotResponse> getSlotById(@PathVariable UUID slotId) {
        return ResponseEntity.ok(slotServices.getSlotById(slotId));
    }

    @GetMapping("/available/{doctorId}")
    public ResponseEntity<List<SlotResponse>> getAvailableSlots(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(slotServices.getAvailableSlots(doctorId));
    }

    @PostMapping("/{slotId}/close")
    public ResponseEntity<Void> closeSlot(@PathVariable UUID slotId) {
        slotServices.closeSlot(slotId);
        return ResponseEntity.noContent().build();
    }
}
