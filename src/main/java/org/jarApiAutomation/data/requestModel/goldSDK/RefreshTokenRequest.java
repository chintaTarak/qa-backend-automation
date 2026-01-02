package org.jarApiAutomation.data.requestModel.goldSDK;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenRequest {

    private String refreshToken;
    private String accessToken;

    public static RefreshTokenRequest createToken(String refreshToken, String accessToken) {
        return RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .build();
    }
}
