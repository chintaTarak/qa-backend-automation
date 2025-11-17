package validations.auth;

import org.JarApiAutomation.data.responseModel.auth.RequestOtpResponse;
import org.JarApiAutomation.data.responseModel.auth.VerifyOtpResponse;
import org.testng.asserts.SoftAssert;
import validations.ApiAssertions;

public class AuthValidation extends ApiAssertions {

    public AuthValidation(SoftAssert softAssert) {
        super(softAssert);
    }

    public void assertRequestOtp(RequestOtpResponse requestOtpResponse) {
        assertHttpSuccess(requestOtpResponse.getStatusCode(), "Request Otp");
        softAssert.assertNotNull(requestOtpResponse.getData().getReqId(), "ReqId is Not Null");
    }

    public void assertVerifyOtp(VerifyOtpResponse verifyOtpResultModel) {
        assertHttpSuccess(verifyOtpResultModel.getStatusCode(), "Request Otp");
        softAssert.assertNotNull(verifyOtpResultModel.getData().getAccessToken(), "AccessToken is Not Null");
    }
}
