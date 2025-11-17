package test.auth;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import main.AuthMethods;
import org.JarApiAutomation.dbConfiguration.MongoDBConstants;
import org.JarApiAutomation.data.requestModel.auth.VerifyOtpRequest;
import org.JarApiAutomation.data.responseModel.auth.RequestOtpResponse;
import org.JarApiAutomation.data.responseModel.auth.VerifyOtpResponse;
import org.JarApiAutomation.utils.TestData;
import org.bson.Document;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import validations.auth.AuthValidation;
import java.util.Map;
import static org.JarApiAutomation.dbConfiguration.MongoDBUtils.fetchDataFromAuth;

@Slf4j
public class AuthService {

    SoftAssert softAssert = new SoftAssert();
    private final AuthMethods authMethods = new AuthMethods();
    private final AuthValidation authValidation = new AuthValidation(softAssert);
    private static String reqId;

    // Request OTP with Valid PhoneNumber and get reqId
    @Test(priority = 1, dataProvider = "invalidPhoneNumbers", dataProviderClass = AuthDataProvider.class)
    public void requestOtp(String phoneNumber) {
        try {
            RequestOtpResponse requestOtpResponse = authMethods.requestOTP(
                    Map.of("countryCode", TestData.COUNTRY_CODE,
                            "phoneNumber", phoneNumber));
            reqId = requestOtpResponse.getData().getReqId();
            log.info("Request ID: {}", reqId);
            authValidation.assertRequestOtp(requestOtpResponse);
        }
        catch(Exception e) {
            log.error("Exception during Request OTP: ", e);
            softAssert.fail("Request OTP test failed due to exception: " + e.getMessage());
        }
    }

    @Test(enabled = false)
    public void decryptOtp() {
        // Verify OTP and get access token
        try {
            Document document = fetchDataFromAuth(
                    MongoDBConstants.DATABASE_NAME,
                    MongoDBConstants.COLLECTION_NAME,
                    MongoDBConstants.GET_OTP_KEY,
                    MongoDBConstants.COUNTRY_CODE + MongoDBConstants.PHONE_NUMBER,
                    MongoDBConstants.SORT_FIELD);
            String encryptedOtp = document.getString("otp");
            log.info("OTP: {}", encryptedOtp);
            Response decryptOtpResponse = authMethods.decryptOtp(
                    Map.of("phoneNumber", TestData.PHONE_NUMBER,
                           "encryptedOTP", encryptedOtp));
            String decryptedOtp = decryptOtpResponse.jsonPath().getString("otp");
            log.info("Decrypted OTP: {}", decryptedOtp);
        }
        catch (Exception e) {
            log.error("Exception during Validate OTP: ", e);
            softAssert.fail("Verify OTP test failed due to exception: " + e.getMessage());
        }
    }

    @Test(priority = 2)
    public void validateOtp() {
        // Extract Access Token
        try {
            VerifyOtpRequest verifyOtpReq = VerifyOtpRequest.verifyOtpPayload(
                    TestData.COUNTRY_CODE,
                    TestData.PHONE_NUMBER,
                    TestData.OTP, reqId);
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