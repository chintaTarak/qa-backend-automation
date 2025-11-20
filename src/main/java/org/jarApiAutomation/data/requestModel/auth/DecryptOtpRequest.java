package org.jarApiAutomation.data.requestModel.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecryptOtpRequest {
    private String deviceId;
}
