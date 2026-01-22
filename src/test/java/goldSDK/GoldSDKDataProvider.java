package goldSDK;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jarApiAutomation.data.requestModel.goldSDK.InitiateKycRequest;
import org.testng.annotations.DataProvider;

import java.util.Map;

import static org.jarApiAutomation.data.requestModel.goldSDK.RefreshTokenRequest.createToken;
import static testData.goldSDK.GoldSDKTestData.*;
import static goldSDK.GoldSDKDataProvider.ExpectedError.*;
import static org.jarApiAutomation.data.requestModel.goldSDK.CreateUserRequest.createUser;

public class GoldSDKDataProvider {
    @AllArgsConstructor
    @Getter
    enum ExpectedError {

        ACCESS_DENIED("20007", "Access denied", 403),
        UNAUTHORIZE("401", "Authentication failed", 401),
        BAD_REQUEST("50003", "User id or phone number is required", 400),
        USER_AUTH_BAD_REQUEST("50001", "Required query parameter userId is not present", 400);
        private final String errorCode;
        private final String errorMessage;
        private final int expectedStatusCode;
    }

    @DataProvider(name = "userCreationScenarios")
    public Object[][] userDetails() {
        return new Object[][]{
                // Success case
                {createUser(USER_REF_ID, USER_FIRST_NAME, USER_LAST_NAME, USER_PHONE_NUMBER, USER_COUNTRY_CODE), X_API_USER, null},
//                // Invalid x-api-key
//                {createUser(USER_REF_ID, USER_FIRST_NAME, USER_LAST_NAME, USER_PHONE_NUMBER, USER_COUNTRY_CODE), INVALID_X_API_USER, ExpectedError.ACCESS_DENIED},
//                // Empty x-api-key
//                {createUser(USER_REF_ID, USER_FIRST_NAME, USER_LAST_NAME, USER_PHONE_NUMBER, USER_COUNTRY_CODE), EMPTY_X_API_USER, ExpectedError.UNAUTHORIZE},
//                // Missing mandatory userRefId
//                {createUser(EMPTY_USER_REF_ID, USER_FIRST_NAME, USER_LAST_NAME, USER_PHONE_NUMBER, USER_COUNTRY_CODE), X_API_USER, ExpectedError.BAD_REQUEST},
        };
    }
    @DataProvider(name = "userAuthDetails")
    public Object[][] userAuthDetails() {
        return new Object[][]{
                // Success case
                {X_API_USER, null, null},
//                // Empty x-api-key
//                {EMPTY_USER_REF_ID, Map.of("userId", USER_ID), ExpectedError.UNAUTHORIZE},
//                // Invalid x-api-key
//                {INVALID_X_API_USER, Map.of("userId", INVALID_USER_ID), ExpectedError.ACCESS_DENIED},
//                // Missing userId query parameter
//                {X_API_USER, null, ExpectedError.USER_AUTH_BAD_REQUEST},
        };
    }

    @DataProvider(name = "getUserScenarios")
    public Object[][] getUserDetails() {
        return new Object[][]{
                //Fetch user details with valid userId
                {null},
//                //Fetch user details with empty userId
//                {Map.of("userId", EMPTY_USER_REF_ID), null},
//                //Fetch user details with invalid userId
//                {Map.of("userId", INVALID_USER_ID), null},
        };
    }

    @DataProvider(name = "getRefreshToken")
    public Object[][] getRefreshToken() {
        return new Object[][]{
                // Success case
                {createToken(GoldSDKTest.refreshToken, GoldSDKTest.accessToken), null},
//                // Empty refresh token
//                {createToken(EMPTY_REFRESH_TOKEN, GoldSDKTest.accessToken), ExpectedError.BAD_REQUEST},
//                // Empty access token
//                {createToken(GoldSDKTest.refreshToken, EMPTY_ACCESS_TOKEN), ExpectedError.BAD_REQUEST},
//                // Both refresh token and access token empty
//                {createToken(EMPTY_REFRESH_TOKEN, EMPTY_ACCESS_TOKEN), ExpectedError.BAD_REQUEST},
        };
    }
    @DataProvider(name = "initiateKycScenarios")
    public Object[][] initiateKycScenarios() {
        return new Object[][]{
                //  Happy path
                {new InitiateKycRequest(USER_PHONE_NUMBER, USER_COUNTRY_CODE, new InitiateKycRequest.PanVerificationDoc(KYC_DOC_TYPE, DOC_NUMBER, null, USER_FIRST_NAME,DOB)), null},
                // Missing docFrontImageId
//                {new InitiateKycRequest(USER_PHONE_NUMBER, USER_COUNTRY_CODE, new InitiateKycRequest.PanVerificationDoc(KYC_DOC_TYPE, DOC_NUMBER, null, USER_FIRST_NAME, DOB)),ExpectedError.BAD_REQUEST}
        };
    }
    @DataProvider(name = "getKycStatusScenarios")
    public Object[][] getKycStatusScenarios() {
        return new Object[][]{
                {null}, // success
                // {ExpectedError.UNAUTHORIZE},
                // {ExpectedError.ACCESS_DENIED}
        };
    }


}
