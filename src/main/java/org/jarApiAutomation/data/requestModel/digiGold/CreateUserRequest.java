package org.jarApiAutomation.data.requestModel.digiGold;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String countryCode;
    private boolean isKycCompleted;
    private String userRefId;

    public static CreateUserRequest createUser(String firstName, String lastName, String phoneNumber,
                                               String countryCode,
                                               String userRefId) {
        return CreateUserRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .countryCode(countryCode).userRefId(userRefId)
                .build();
    }
}
