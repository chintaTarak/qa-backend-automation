package changejar.authService;

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

    @Test(priority = 1, dataProvider = "invalidPhoneNumbers", dataProviderClass = AuthDataProvider.class)
    public void validateRequestOtp(String phoneNumber) {
        try {

            RequestOtpResponse response = authMethods.requestOTP(Map.of("phoneNumber",phoneNumber));
            if(response.isSuccess() && response.getData() != null) {
                reqId = response.getData().getReqId();
                log.info("Feteched Otp: {}", reqId);
            }
            authValidation.assertRequestOtp(response);

        } catch (Exception e) {
            log.error("Exception during Session Validation: {}", e.getMessage());
            softAssert.fail(
                    "Validate Previous Session Validation Failed due to exception: "
                            + e.getMessage());
        }
    }
    @Test(priority = 2,dataProvider = "invalidPhoneNumbers", dataProviderClass = AuthDataProvider.class)
        public void validateFetchOtp(String phoneNumber) {
            try {
                FetchOtpResponse response = authMethods.fetchOtp(Map.of("phoneNumber", phoneNumber), Map.of("Admintoken", ADMIN_TOKEN));
                if(response.isSuccess() && response.getData() != null) {
                    otp = response.getData();
                    log.info("Feteched Otp: {}", otp);
                }
                authValidation.assertFetchOtp(response);
            } catch (Exception e) {
                log.error("Exception during Session Validation: {}", e.getMessage());
                softAssert.fail(
                        "Validate Previous Session Validation Failed due to exception: "
                                + e.getMessage());
            }
        }

    @Test(priority = 3,dataProvider = "invalidPhoneNumbers", dataProviderClass = AuthDataProvider.class)
    public void validateVerifyOtp(String phoneNumber) {
        try {
            VerifyOtpRequest verify = VerifyOtpRequest.verifyPayload(otp,phoneNumber,reqId);
            VerifyOtpResponse response = authMethods.verifyOtp(verify);
            authValidation.assertVerifyOtp(response);
        } catch (Exception e) {
            log.error("Exception during Session Validation: {}", e.getMessage());
            softAssert.fail(
                    "Validate Previous Session Validation Failed due to exception: "
                            + e.getMessage());
        }
    }


}
