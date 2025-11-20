package authService;

import org.jarApiAutomation.data.responseModel.auth.RequestOtpResponse;
import org.jarApiAutomation.data.responseModel.auth.VerifyOtpResponse;
import org.testng.asserts.SoftAssert;
import org.jarApiAutomation.utils.ApiAssertions;

public class AuthValidation extends ApiAssertions {

    public AuthValidation(SoftAssert softAssert)
    {
        super(softAssert);
    }

    public void assertRequestOtp(RequestOtpResponse requestOtpResponse) {
        assertHttpSuccess(requestOtpResponse.getStatusCode(), "Request Otp");
        softAssert.assertNull(requestOtpResponse.getData(), "Data should be null when OTP fails");
    }

    public void assertVerifyOtp(VerifyOtpResponse verifyOtpResultModel) {
        assertHttpSuccess(verifyOtpResultModel.getStatusCode(), "Request Otp");
        softAssert.assertNotNull(verifyOtpResultModel.getData().getAccessToken(), "AccessToken is Not Null");
    }
}
