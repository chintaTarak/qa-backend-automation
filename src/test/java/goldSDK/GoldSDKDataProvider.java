package goldSDK;

import static goldSDK.GoldSDKTest.userId;
import static org.jarApiAutomation.data.requestModel.goldSDK.CreateUserRequest.createUser;
import static org.jarApiAutomation.data.requestModel.goldSDK.RefreshTokenRequest.createToken;
import static org.jarApiAutomation.data.requestModel.goldSDK.AutoPayInitiateRequest.initiateAutoPayRequest;
import static testData.goldSDK.GoldSDKTestData.*;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jarApiAutomation.data.requestModel.goldSDK.InitiateAadhaarKycRequest;
import org.jarApiAutomation.data.requestModel.goldSDK.InitiatePanKycRequest;
import org.jarApiAutomation.utils.CommonUtil;
import org.testng.annotations.DataProvider;

public class GoldSDKDataProvider {
    @AllArgsConstructor
    @Getter
    enum ExpectedError {
        ACCESS_DENIED("20007", "Access denied", 403),
        UNAUTHORIZE("401", "Authentication failed", 401),
        BAD_REQUEST("50003", "User id or phone number is required", 400),
        EMPTY_REF_ID_BAD_REQUEST("50003", "Field userRefId should be present.", 400),
        USER_AUTH_BAD_REQUEST("50001", "Required query parameter userId is not present", 400),
        DOES_NOT_HAVE_EXISTS("403", "Authentication failed", 403),
        EMPTY_REFRSHTOKEN_BAD_REQUEST("50003", "Field refreshToken should be present.", 400),
        EMPTY_ACCESSTOKEN_BAD_REQUEST("50003", "Field accessToken should be present", 400),
        EMPTY_TOKEN_BAD_REQUEST("50003", "Field refreshToken should be present., Field accessToken should be present.", 400);

        private final String errorCode;
        private final String errorMessage;
        private final int expectedStatusCode;
    }

    @DataProvider(name = "userCreationScenarios")
    public Object[][] userDetails() {
        String userRefId = CommonUtil.generateMongoId();
        return new Object[][] {
                // Success case
                {
                        createUser(
                                userRefId,
                                USER_FIRST_NAME,
                                USER_LAST_NAME,
                                USER_PHONE_NUMBER,
                                USER_COUNTRY_CODE),
                        X_API_USER,
                        null
                },
                // Invalid x-api-key
                {
                        createUser(
                                userRefId,
                                USER_FIRST_NAME,
                                USER_LAST_NAME,
                                USER_PHONE_NUMBER,
                                USER_COUNTRY_CODE),
                        INVALID_X_API_USER,
                        ExpectedError.ACCESS_DENIED
                },
                // Empty x-api-key
                {
                        createUser(
                                userRefId,
                                USER_FIRST_NAME,
                                USER_LAST_NAME,
                                USER_PHONE_NUMBER,
                                USER_COUNTRY_CODE),
                        EMPTY_X_API_USER,
                        ExpectedError.UNAUTHORIZE
                },
                // Missing mandatory userRefId
                {
                        createUser(
                                EMPTY_USER_REF_ID,
                                USER_FIRST_NAME,
                                USER_LAST_NAME,
                                USER_PHONE_NUMBER,
                                USER_COUNTRY_CODE),
                        X_API_USER,
                        ExpectedError.EMPTY_REF_ID_BAD_REQUEST
                },
        };
    }

    @DataProvider(name = "userAuthDetails")
    public Object[][] userAuthDetails() {
        return new Object[][] {
                // Success case
                {X_API_USER, Map.of("userId", userId), null},
                // Empty x-api-key
                {EMPTY_USER_REF_ID, Map.of("userId", userId), ExpectedError.UNAUTHORIZE},
                // Invalid x-api-key
                {INVALID_X_API_USER, Map.of("userId", INVALID_USER_ID), ExpectedError.ACCESS_DENIED},
                // Missing userId query parameter
                {X_API_USER, null, ExpectedError.USER_AUTH_BAD_REQUEST},
        };
    }

    @DataProvider(name = "getUserScenarios")
    public Object[][] getUserDetails() {
        return new Object[][] {
                // Fetch user details with valid userId
                {Map.of("userId", userId), null},
                // Fetch user details with empty userId
                {Map.of("userId", EMPTY_USER_REF_ID), null},
                // Fetch user details with invalid userId
                {Map.of("userId", INVALID_USER_ID), null},
        };
    }

    @DataProvider(name = "getRefreshToken")
    public Object[][] getRefreshToken() {
        return new Object[][] {
                // Success case
                {createToken(GoldSDKTest.refreshToken, GoldSDKTest.accessToken), null},
                // Empty refresh token
                {createToken(EMPTY_REFRESH_TOKEN, GoldSDKTest.accessToken), ExpectedError.EMPTY_REFRSHTOKEN_BAD_REQUEST},
                // Empty access token
                {createToken(GoldSDKTest.refreshToken, EMPTY_ACCESS_TOKEN), ExpectedError.EMPTY_ACCESSTOKEN_BAD_REQUEST},
                // Both refresh token and access token empty
                {createToken(EMPTY_REFRESH_TOKEN, EMPTY_ACCESS_TOKEN), ExpectedError.EMPTY_TOKEN_BAD_REQUEST},
        };
    }

    @DataProvider(name = "initiateAutoPay")
    public Object[][] initiateAutoPay() {
        return new Object[][] {
                // valid case
                {initiateAutoPayRequest(GoldSDKTest.frequency,MANDATE_AMOUNT,MAX_MANDATE_AMOUNT,PACKAGE_NAME),Map.of("Authorization","Bearer "+ GoldSDKTest.accessToken),null},
        };
    }
    @DataProvider(name = "getBuyPriceSDKScenarios")
    public Object[][] getBuyPriceSDKScenarios() {
        return new Object[][]
                {
                        {null, ExpectedError.UNAUTHORIZE},
                        {"", ExpectedError.UNAUTHORIZE},
                        {INVALID_ACCESS_TOKEN, ExpectedError.DOES_NOT_HAVE_EXISTS},
                        {GoldSDKTest.accessToken, null}
               };
    }



    @DataProvider(name = "uploadDocTypes")
    public Object[][] uploadDocTypes() {
        return new Object[][]{
                {GoldSDKTest.accessToken,"PAN"},
                {GoldSDKTest.accessToken,"AADHAAR"}
        };
    }
    @DataProvider(name = "initiateKycScenarios")
    public Object[][] initiateKycScenarios() {
        return new Object[][]{
                // -------- PAN FLOW --------
                {
                        new InitiatePanKycRequest(
                                USER_PHONE_NUMBER,
                                USER_COUNTRY_CODE,
                                new InitiatePanKycRequest.panVerificationDoc(
                                        KYC_PAN_DOC_TYPE,
                                        PAN_DOC_NUMBER,
                                        null,
                                        FULL_NAME,
                                        DOB
                                )
                        ),
                        GoldSDKTest.accessToken,
                        null
                },
                // -------- AADHAAR FLOW --------
                {
                        new InitiateAadhaarKycRequest(
                                USER_PHONE_NUMBER,
                                USER_COUNTRY_CODE,
                                new InitiateAadhaarKycRequest.kycVerificationDoc(
                                        KYC_AADHAAR_DOC_TYPE,
                                        KYC_DOC_NUMBER,
                                        null,
                                        FULL_NAME,
                                        DOB
                                )
                        ),
                        GoldSDKTest.accessToken,
                        null
                }
        };
    }

    @DataProvider(name = "kycStatusScenarios")
    public Object[][] kycStatusScenarios() {
        return new Object[][]{
                {GoldSDKTest.accessToken,"PAN", null},
                {GoldSDKTest.accessToken,"AADHAAR", null}
        };
    }
}
