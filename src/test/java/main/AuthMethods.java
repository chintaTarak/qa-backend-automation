package main;

import io.restassured.response.Response;
import org.JarApiAutomation.utils.ApiRequests;
import org.JarApiAutomation.configuration.BaseUri;
import org.JarApiAutomation.endpoints.auth.AuthServiceEndpoints;
import org.JarApiAutomation.data.common.RestRequest;
import org.JarApiAutomation.data.requestModel.auth.ResetOtpRequest;
import org.JarApiAutomation.data.requestModel.auth.VerifyOtpRequest;
import org.JarApiAutomation.data.responseModel.auth.RequestOtpResponse;
import org.JarApiAutomation.data.responseModel.auth.VerifyOtpResponse;
import org.JarApiAutomation.utils.CommonSerializationUtil;
import java.util.Map;

public class AuthMethods {

    private final ApiRequests apiRequests = new ApiRequests();

    public RequestOtpResponse requestOTP(Map<String, Object> queryParams) {
        RestRequest req = RestRequest.builder()
                .headers(Map.of("content-type", "application/json"))
                .url(BaseUri.BASE_URI + AuthServiceEndpoints.V2 + AuthServiceEndpoints.REQUEST_OTP)
                .queryParams(queryParams)
                .build();
        Response response = apiRequests.post(req);
        return CommonSerializationUtil.readObject(response.getBody().asString(), RequestOtpResponse.class);
    }

    public VerifyOtpResponse verifyOtp(VerifyOtpRequest verifyOtpReq) {
        RestRequest req = RestRequest.builder()
                .url(BaseUri.BASE_URI + AuthServiceEndpoints.V2 + AuthServiceEndpoints.VERIFY_OTP)
                .body(verifyOtpReq)
                .build();
        Response response = apiRequests.post(req);
        return CommonSerializationUtil.readObject(response.getBody().asString(), VerifyOtpResponse.class);
    }

    public Response decryptOtp(Map<String, Object> queryParams) {
        RestRequest req = RestRequest.builder()
                .url(BaseUri.BASE_URI + AuthServiceEndpoints.V2 + AuthServiceEndpoints.VERIFY_OTP)
                .queryParams(queryParams)
                .build();
        return apiRequests.get(req);
    }

    public Response resetOtpLimit(String countryCode, String phoneNumber) {
        ResetOtpRequest resetOtp = new ResetOtpRequest();
        resetOtp.setMobileNumber(countryCode + phoneNumber);
        RestRequest req = RestRequest.builder()
                .url(BaseUri.BASE_URI + AuthServiceEndpoints.V1 + AuthServiceEndpoints.RESET_OTP)
                .body(resetOtp)
                .build();
        return apiRequests.post(req);
    }

    public Response verifyOtpLimit(String countryCode, String phoneNumber) {
        ResetOtpRequest resetOtp = new ResetOtpRequest();
        resetOtp.setMobileNumber(countryCode + phoneNumber);
        RestRequest req = RestRequest.builder()
                .url(BaseUri.BASE_URI + AuthServiceEndpoints.V2 + AuthServiceEndpoints.RESET_VERIFY_OTP_LIMIT)
                .body(resetOtp)
                .build();
        return apiRequests.get(req);
    }
}
