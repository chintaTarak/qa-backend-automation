package authService;

import org.jarApiAutomation.data.responseModel.auth.FetchOtpResponse;
import org.jarApiAutomation.data.responseModel.auth.RequestOtpResponse;
import org.jarApiAutomation.data.responseModel.auth.VerifyOtpResponse;
import org.testng.asserts.SoftAssert;
import org.jarApiAutomation.utils.ApiAssertions;

public class AuthValidation extends ApiAssertions {

    public AuthValidation(SoftAssert softAssert)
    {
        super(softAssert);
    }

    public void assertRequestOtp(RequestOtpResponse response) {
        assertHttpSuccess(response.getStatusCode(), "Request Otp");
        // If success == false → this is a failure case
        if (!response.isSuccess()) {
            softAssert.assertNull(response.getData(), "Data must be null when success = false");
            return;
        }
        // If success == true → positive case
        softAssert.assertNotNull(response.getData().getReqId(), "ReqId must not be null.");
        softAssert.assertAll();
    }

    public void assertVerifyOtp(VerifyOtpResponse response) {
        assertHttpSuccess(response.getStatusCode(), "Request Otp");
        softAssert.assertTrue(response.isSuccess(), "VerifyOTP should return true but got false");
        // If success=true, then validate fields
        softAssert.assertNotNull(response.getData(), "Data must not be null when success=true");
        softAssert.assertNotNull(response.getData().getAccessToken(), "AccessToken must not be null");
        softAssert.assertAll();
    }

    public void assertFetchOtp(FetchOtpResponse response) {
        assertHttpSuccess(response.getStatusCode(), "Fetch Otp");
        softAssert.assertTrue(response.isSuccess(), "Fetch OTP failed");
        // If success=true, then validate fields
        softAssert.assertNotNull(response.getData(), "Otp should not be null");
        softAssert.assertAll();
    }
}
