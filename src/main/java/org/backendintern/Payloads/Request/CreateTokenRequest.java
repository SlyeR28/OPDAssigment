package org.backendintern.Payloads.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.backendintern.Models.Enums.TokenType;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTokenRequest {

    @NotBlank(message = "Patient name is required")
   private String patientName;

    @NotNull(message = "Token type is required")
   private TokenType  tokenType;

    // optional : assign to a specific doctor
   private UUID doctorId;


}
