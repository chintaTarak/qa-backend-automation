package authService;

import base.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.jarApiAutomation.data.requestModel.auth.VerifyOtpRequest;
import org.jarApiAutomation.data.responseModel.auth.FetchOtpResponse;
import org.jarApiAutomation.data.responseModel.auth.RequestOtpResponse;
import org.jarApiAutomation.data.responseModel.auth.VerifyOtpResponse;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import java.util.Map;
import static testData.Auth.TestDataAuth.*;

@Slf4j
public class AuthServiceTest extends BaseTest
{
    SoftAssert softAssert = new SoftAssert();
    private final AuthMethods authMethods = new AuthMethods();
    private final AuthValidation authValidation = new AuthValidation(softAssert);
    private String reqId;
    private String otp;
    public String accessToken;

    // Request OTP with Valid PhoneNumber and get reqId
    @Test(priority = 1, description = "To Request OTP", dataProvider = "invalidPhoneNumbers", dataProviderClass = AuthDataProvider.class)
    public void requestOtp(String phoneNumber) {
        try {
            RequestOtpResponse requestOtpResponse =  authMethods.requestOTP(
                    Map.of("countryCode", COUNTRY_CODE,
                            "phoneNumber", phoneNumber));
            if (requestOtpResponse.isSuccess() && requestOtpResponse.getData() != null) {
                reqId = requestOtpResponse.getData().getReqId();
                log.info("Request ID: {}", reqId);
            }
            authValidation.assertRequestOtp(requestOtpResponse);
        }
        catch(Exception e)
        {
            log.error("Exception during Request OTP: {}", e.getMessage());
            softAssert.fail("Request OTP test failed due to exception: " + e.getMessage());
            softAssert.assertAll();
        }
    }

    @Test(priority = 2, description = "Fetching OTP From Database")
    public void fetchOtp() {
        // Verify OTP and get access token
        try {
            FetchOtpResponse fetchOtpResponse = authMethods.fetchOtp(
                    Map.of("phoneNumber", PHONE_NUMBER),
                    Map.of("Authorization", ADMIN_TOKEN));
            if (fetchOtpResponse.isSuccess() && fetchOtpResponse.getData() != null) {
                otp = fetchOtpResponse.getData();
                log.info("Fetched OTP: {}", otp);
            }
            authValidation.assertFetchOtp(fetchOtpResponse);
        }
        catch (Exception e) {
            log.error("Failed to fetch OTP: {}", e.getMessage());
            softAssert.fail("Fetch OTP test failed due to exception: " + e.getMessage());
            softAssert.assertAll();
        }
    }

    @Test(priority = 3, description = "Validating the OTP to Get Access Token")
    public void validateOtp() {
        // Extract Access Token
        try {
            VerifyOtpRequest verifyOtpReq = VerifyOtpRequest.verifyOtpPayload(
                    COUNTRY_CODE,
                    PHONE_NUMBER,
                    otp, reqId);
            VerifyOtpResponse verifyOtpResultModel = authMethods.verifyOtp(verifyOtpReq);
            if (verifyOtpResultModel.isSuccess() && verifyOtpResultModel.getData() != null) {
                accessToken = verifyOtpResultModel.getData().getAccessToken();
                log.info("AccessToken: {}", accessToken);
            }
            authValidation.assertVerifyOtp(verifyOtpResultModel);
        } catch (Exception e) {
            log.error("Exception during Validate OTP: {}", e.getMessage());
            softAssert.fail("Verify OTP test failed due to exception: " + e.getMessage());
            softAssert.assertAll();
        }
    }
}