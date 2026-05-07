package changejar.authService;

import static testData.Auth.TestDataAuth.*;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.jarApiAutomation.data.requestModel.auth.VerifyOtpRequest;
import org.jarApiAutomation.data.responseModel.auth.FetchOtpResponse;
import org.jarApiAutomation.data.responseModel.auth.RequestOtpResponse;
import org.jarApiAutomation.data.responseModel.auth.VerifyOtpResponse;

@Slf4j
public class AuthServiceHelper {

    private final AuthMethods authMethods = new AuthMethods();

    /**
     * Generates Auth Token using OTP flow
     * Stores token in TestNG Suite Context
     */
    public String generateToken(ITestContext context) {
        Object existingToken = context.getSuite().getAttribute("AUTH_TOKEN");
        if (existingToken != null) {
            log.info("AUTH_TOKEN already exists in Suite Context");
            return existingToken.toString();
        }

        // Request OTP
        RequestOtpResponse requestOtpResponse =
                authMethods.requestOTP(
                        Map.of("phoneNumber", TEST_PHONE_NUMBER));

        if (!requestOtpResponse.isSuccess() || requestOtpResponse.getData() == null) {
            throw new RuntimeException("Request OTP failed");
        }

        String reqId = requestOtpResponse.getData().getReqId();
        log.info("Request OTP successful, reqId={}", reqId);

        //Fetch OTP (Admin API)
        FetchOtpResponse fetchOtpResponse =
                authMethods.fetchOtp(
                        Map.of("phoneNumber", TEST_PHONE_NUMBER),
                        Map.of("Authorization", ADMIN_TOKEN));

        if (!fetchOtpResponse.isSuccess() || fetchOtpResponse.getData() == null) {
            throw new RuntimeException("Fetch OTP failed");
        }

        String otp = fetchOtpResponse.getData();
        log.info("Fetched OTP from DB");

        // Verify OTP
        VerifyOtpRequest verifyOtpRequest =
                VerifyOtpRequest.verifyPayload(otp, TEST_PHONE_NUMBER, reqId);

        VerifyOtpResponse verifyOtpResponse =
                authMethods.verifyOtp(verifyOtpRequest);

        if (!verifyOtpResponse.isSuccess()
                || verifyOtpResponse.getData() == null
                || verifyOtpResponse.getData().getAccessToken() == null) {
            throw new RuntimeException("Verify OTP failed");
        }

        String accessToken = verifyOtpResponse.getData().getAccessToken();
        log.info("Access token generated successfully");

        //Store token in Suite Context
        context.getSuite().setAttribute("AUTH_TOKEN", accessToken);

        return accessToken;
    }
}
