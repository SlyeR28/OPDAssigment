package org.backendintern.Payloads.Request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CreateSlotRequest {

    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;

    @NotNull(message = "Start Time is required")
    @Future(message = "Start Time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End Time is required")
    @Future(message = "End Time must be in the future")
    private LocalDateTime endTime;

    @Min(value = 1, message = "Max capacity must be at least 1")
    @Max(value = 20, message = "Max capacity cannot exceed 20")
    private int maxCapacity;

}
