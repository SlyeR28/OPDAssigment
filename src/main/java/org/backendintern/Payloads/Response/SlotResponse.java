package org.backendintern.Payloads.Response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SlotResponse {

    private UUID slotId;
    private UUID doctorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int maxCapacity;
    private int currentCapacity;
    private boolean full;

}
