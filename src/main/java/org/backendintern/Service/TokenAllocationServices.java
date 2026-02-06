package org.backendintern.Service;

import org.backendintern.Payloads.Request.CreateTokenRequest;
import org.backendintern.Payloads.Response.TokenResponse;

import java.util.List;
import java.util.UUID;

public interface TokenAllocationServices {

    //Allocate Token
    TokenResponse allocateToken(CreateTokenRequest createTokenRequest);

    // cancel token
    void cancelToken(UUID tokenId);

    // handle no-show for a token
    void handleNoShowToken(UUID tokenId);

    // get tokens by slot
    List<TokenResponse> getTokensBySlot(UUID slotId);

    // get tokens for a doctor
    List<TokenResponse> getAllTokensforDoctors(UUID doctorId);


}
