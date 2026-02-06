package org.backendintern.Service;

import org.backendintern.Payloads.Request.CreateSlotRequest;
import org.backendintern.Payloads.Response.SlotResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SlotServices {

    SlotResponse createSlot(CreateSlotRequest createSlotRequest);

    SlotResponse getSlotById(UUID slotId);

    List<SlotResponse> getAvailableSlots(UUID doctorId);

    void closeSlot(UUID slotId);

    SlotResponse findBestEmergencySlot(LocalDateTime currentTime);


}
