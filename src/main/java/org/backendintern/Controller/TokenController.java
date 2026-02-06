package org.backendintern.Controller;

import lombok.RequiredArgsConstructor;
import org.backendintern.Payloads.Request.CreateTokenRequest;
import org.backendintern.Payloads.Response.TokenResponse;
import org.backendintern.Service.TokenAllocationServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
public class TokenController {

    private final TokenAllocationServices tokenAllocationServices;

    @PostMapping("/book")
    public ResponseEntity<TokenResponse> bookToken(@RequestBody CreateTokenRequest request) {
        TokenResponse response = tokenAllocationServices.allocateToken(request);
        if (response == null) {
            return ResponseEntity.accepted().build(); // Waitlisted
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{tokenId}")
    public ResponseEntity<Void> cancelToken(@PathVariable UUID tokenId) {
        tokenAllocationServices.cancelToken(tokenId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tokenId}/no-show")
    public ResponseEntity<Void> handleNoShow(@PathVariable UUID tokenId) {
        tokenAllocationServices.handleNoShowToken(tokenId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<TokenResponse>> getTokensByDoctor(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(tokenAllocationServices.getAllTokensforDoctors(doctorId));
    }

    @GetMapping("/slot/{slotId}")
    public ResponseEntity<List<TokenResponse>> getTokensBySlot(@PathVariable UUID slotId) {
        return ResponseEntity.ok(tokenAllocationServices.getTokensBySlot(slotId));
    }
}
