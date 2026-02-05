package org.jarApiAutomation.data.requestModel.goldSDK;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserRequest {

    private String userRefId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String countryCode;

    public static CreateUserRequest createUser(
            String userRefId,
            String firstName,
            String lastName,
            String phoneNumber,
            String countryCode) {
        return CreateUserRequest.builder()
                .userRefId(userRefId)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .countryCode(countryCode)
                .build();
    }
}
