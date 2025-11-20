package authService;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.jarApiAutomation.data.requestModel.auth.VerifyOtpRequest;
import org.jarApiAutomation.data.responseModel.auth.RequestOtpResponse;
import org.jarApiAutomation.data.responseModel.auth.VerifyOtpResponse;
import testData.Auth.TestDataAuth;
import org.bson.Document;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.Map;

import static org.jarApiAutomation.dbConfiguration.MongoDBConstants.*;
import static org.jarApiAutomation.dbConfiguration.MongoDBUtils.fetchDataFromAuth;
import static testData.Auth.TestDataAuth.*;


@Slf4j
public class AuthServiceTest
{

    SoftAssert softAssert = new SoftAssert();
    private final AuthMethods authMethods = new AuthMethods();
    private final AuthValidation authValidation = new AuthValidation(softAssert);
    private static String reqId;
    private String decryptedOtp;



    // Request OTP with Valid PhoneNumber and get reqId
    @Test(priority = 1, dataProvider = "invalidPhoneNumbers", dataProviderClass = AuthDataProvider.class)
    public void requestOtp(String phoneNumber) {
        try {
            RequestOtpResponse requestOtpResponse =  authMethods.requestOTP(
                    Map.of("countryCode", COUNTRY_CODE,
                            "phoneNumber", phoneNumber));
            reqId = requestOtpResponse.getData().getReqId();
            log.info("Request ID: {}", reqId);
            authValidation.assertRequestOtp(requestOtpResponse);
        }
        catch(Exception e)
        {
            log.error("Exception during Request OTP: ", e);
            softAssert.fail("Request OTP test failed due to exception: " + e.getMessage());
        }
    }

    @Test(enabled = false)
    public void decryptOtp() {
        // Verify OTP and get access token
        try {
            Document document = fetchDataFromAuth(
                    AUTH_DB,
                    SMS_DELIVERY_REPORTS,
                    FILTER_KEY,
              COUNTRY_CODE_DB + MOBILE_NUMBER, SORT_FIELD);
            String encryptedOtp = document.getString("otp");
            log.info("OTP: {}", encryptedOtp);
            Response decryptOtpResponse = authMethods.decryptOtp(
                    Map.of("phoneNumber", TestDataAuth.PHONE_NUMBER_ADMIN,
                           "encryptedOTP", encryptedOtp));
            String decryptedOtp = decryptOtpResponse.jsonPath().getString("otp");
            log.info("Decrypted OTP: {}", decryptedOtp);
        }
        catch (Exception e) {
            log.error("Exception during Validate OTP: ", e);
            softAssert.fail("Verify OTP test failed due to exception: " + e.getMessage());
        }
    }

    @Test(priority = 2,enabled = false)
    public void validateOtp() {
        // Extract Access Token
        try {
            VerifyOtpRequest verifyOtpReq = VerifyOtpRequest.verifyOtpPayload(
                    COUNTRY_CODE,
                    PHONE_NUMBER,
                    TestDataAuth.OTP, reqId);
            VerifyOtpResponse verifyOtpResultModel = authMethods.verifyOtp(verifyOtpReq);
            String accessToken = verifyOtpResultModel.getData().getAccessToken();
            log.info("AccessToken: {}", accessToken);
            authValidation.assertVerifyOtp(verifyOtpResultModel);
        } catch (Exception e) {
            log.error("Exception during Validate OTP: ", e);
            softAssert.fail("Verify OTP test failed due to exception: " + e.getMessage());
        }
    }
}