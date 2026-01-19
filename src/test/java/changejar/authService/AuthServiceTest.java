package changejar.authService;

import static changejar.ApiErrorCodes.USER_LOGGED_OUT;
import static testData.Auth.TestDataAuth.*;

import base.BaseTest;
import changejar.userProfile.UserMethods;
import changejar.userProfile.UserValidation;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jarApiAutomation.data.requestModel.auth.VerifyOtpRequest;
import org.jarApiAutomation.data.responseModel.auth.FetchOtpResponse;
import org.jarApiAutomation.data.responseModel.auth.RequestOtpResponse;
import org.jarApiAutomation.data.responseModel.auth.VerifyOtpResponse;
import org.jarApiAutomation.data.responseModel.userProfile.UserDetailsResponse;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

@Slf4j
public class AuthServiceTest extends BaseTest {
    protected SoftAssert softAssert = new SoftAssert();
    private final AuthMethods authMethods = new AuthMethods();
    private final UserMethods userMethods = new UserMethods();
    private AuthValidation authValidation;
    private UserValidation userValidation;

    @BeforeMethod
    public void setup() {
        softAssert = new SoftAssert();
        authValidation = new AuthValidation(softAssert);
        userValidation = new UserValidation(softAssert);
    }

    private String reqId;
    private String otp;
    public static String accessToken;

    public static String userId;

    // Request OTP with Valid PhoneNumber and get reqId
    @Test(
            priority = 1,
            description = "To Request OTP",
            dataProvider = "invalidPhoneNumbers",
            dataProviderClass = AuthDataProvider.class)
    public void requestOtp(String phoneNumber) {
        try {
            RequestOtpResponse requestOtpResponse =
                    authMethods.requestOTP(
                            Map.of("countryCode", COUNTRY_CODE, "phoneNumber", phoneNumber));
            if (requestOtpResponse.isSuccess() && requestOtpResponse.getData() != null) {
                reqId = requestOtpResponse.getData().getReqId();
                log.info("Request ID: {}", reqId);
            }
            authValidation.assertRequestOtp(requestOtpResponse);
        } catch (Exception e) {
            log.error("Exception during Request OTP: {}", e.getMessage());
            softAssert.fail("Request OTP test failed due to exception: " + e.getMessage());
            softAssert.assertAll();
        }
    }

    @Test(priority = 2, description = "Fetching OTP From Database")
    public void fetchOtp() {
        try {
            FetchOtpResponse fetchOtpResponse =
                    authMethods.fetchOtp(
                            Map.of("phoneNumber", TEST_PHONE_NUMBER),
                            Map.of("Authorization", ADMIN_TOKEN));
            if (fetchOtpResponse.isSuccess() && fetchOtpResponse.getData() != null) {
                otp = fetchOtpResponse.getData();
                log.info("Fetched OTP: {}", otp);
            }
            authValidation.assertFetchOtp(fetchOtpResponse);
        } catch (Exception e) {
            log.error("Failed to fetch OTP: {}", e.getMessage());
            softAssert.fail("Fetch OTP test failed due to exception: " + e.getMessage());
            softAssert.assertAll();
        }
    }

    @Test(priority = 3, description = "Validating the OTP to Get Access Token")
    public void validateOtp(ITestContext context) {
        try {
            VerifyOtpRequest verifyOtpReq =
                    VerifyOtpRequest.verifyOtpPayload(COUNTRY_CODE, TEST_PHONE_NUMBER, otp, reqId);
            VerifyOtpResponse verifyOtpResultModel = authMethods.verifyOtp(verifyOtpReq);
            if (verifyOtpResultModel.isSuccess() && verifyOtpResultModel.getData() != null) {
                accessToken = verifyOtpResultModel.getData().getAccessToken();
                userId = verifyOtpResultModel.getData().getUser().getUserId();
                log.info("AccessToken: {}", accessToken);

                context.getSuite()
                        .setAttribute("AUTH_TOKEN", accessToken); // Storing token in TestNG Context
            }
            authValidation.assertVerifyOtp(verifyOtpResultModel);
        } catch (Exception e) {
            log.error("Exception during Validate OTP: {}", e.getMessage());
            softAssert.fail("Verify OTP test failed due to exception: " + e.getMessage());
        }
    }

    @Test(
            priority = 4,
            description = "Validating session invalidation after creation of a new session")
    public void validateOldAuthSession() {
        try {
            Map<String, Object> fetchOtpQueryParams =
                    Map.of("countryCode", COUNTRY_CODE, "phoneNumber", PHONE_NUMBER);
            Map<String, Object> phoneNumberQueryParams = Map.of("phoneNumber", PHONE_NUMBER);
            Map<String, String> authorizationHeader = Map.of("Authorization", ADMIN_TOKEN);

            // First Login
            String oldReqId = "";
            String oldOtp = "";
            String oldToken = "";
            RequestOtpResponse old_Response = authMethods.requestOTP(fetchOtpQueryParams);
            oldReqId = old_Response.getData().getReqId();
            FetchOtpResponse fetchOtp_old =
                    authMethods.fetchOtp(phoneNumberQueryParams, authorizationHeader);
            oldOtp = fetchOtp_old.getData();
            VerifyOtpResponse old_Login =
                    authMethods.verifyOtp(
                            VerifyOtpRequest.verifyOtpPayload(
                                    COUNTRY_CODE, PHONE_NUMBER, oldOtp, oldReqId));
            oldToken = old_Login.getData().getAccessToken();

            // Second Login
            String newReqId = "";
            String newOtp = "";
            String newToken = "";
            RequestOtpResponse new_Response = authMethods.requestOTP(fetchOtpQueryParams);
            newReqId = new_Response.getData().getReqId();
            FetchOtpResponse fetchOtp_new =
                    authMethods.fetchOtp(phoneNumberQueryParams, authorizationHeader);
            newOtp = fetchOtp_new.getData();
            VerifyOtpResponse new_Login =
                    authMethods.verifyOtp(
                            VerifyOtpRequest.verifyOtpPayload(
                                    COUNTRY_CODE, PHONE_NUMBER, newOtp, newReqId));
            newToken = new_Login.getData().getAccessToken();

            // Verify Response using the old AccessToken
            UserDetailsResponse userDetailsResponse_old =
                    userMethods.userDetails(Map.of("Authorization", "Bearer " + oldToken));
            userValidation.assertUserDetailsFailureResponse(
                    userDetailsResponse_old,
                    USER_LOGGED_OUT.getErrorCode(),
                    USER_LOGGED_OUT.getErrorMessage());

            // Verify Response using the New AccessToken
            UserDetailsResponse userDetailsResponse_new =
                    userMethods.userDetails(Map.of("Authorization", "Bearer " + newToken));
            userValidation.assertUserDetailsSuccessResponse(userDetailsResponse_new);

        } catch (Exception e) {
            log.error("Exception during Session Validation: {}", e.getMessage());
            softAssert.fail(
                    "Validate Previous Session Validation Failed due to exception: "
                            + e.getMessage());
        } finally {
            softAssert.assertAll();
        }
    }
}
