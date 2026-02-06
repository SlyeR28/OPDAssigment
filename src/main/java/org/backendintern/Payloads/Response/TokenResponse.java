package org.backendintern.Payloads.Response;

import lombok.*;
import org.backendintern.Models.Enums.TokenStatus;
import org.backendintern.Models.Enums.TokenType;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenResponse {

    private UUID tokenId;
    private String patientName;
    private TokenType tokenType;
    private TokenStatus tokenStatus;
    private int score;
    private UUID slotId;
    private UUID doctorId;
    private LocalDateTime createdAt;
}
