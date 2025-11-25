package authService;

import io.restassured.response.Response;
import org.jarApiAutomation.data.responseModel.auth.FetchOtpResponse;
import org.jarApiAutomation.data.responseModel.auth.RequestOtpResponse;
import org.jarApiAutomation.utils.ApiRequests;
import org.jarApiAutomation.configuration.BaseUri;
import org.jarApiAutomation.endpoints.AuthServiceEndpoints;
import org.jarApiAutomation.data.common.RestRequest;
import org.jarApiAutomation.data.requestModel.auth.ResetOtpRequest;
import org.jarApiAutomation.data.requestModel.auth.VerifyOtpRequest;
import org.jarApiAutomation.data.responseModel.auth.VerifyOtpResponse;
import org.jarApiAutomation.utils.CommonSerializationUtil;
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
        RequestOtpResponse deserializedResponse = CommonSerializationUtil.readObject(response.getBody().asString(), RequestOtpResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }

    public VerifyOtpResponse verifyOtp(VerifyOtpRequest verifyOtpReq) {
        RestRequest req = RestRequest.builder()
                .url(BaseUri.BASE_URI + AuthServiceEndpoints.V2 + AuthServiceEndpoints.VERIFY_OTP)
                .body(verifyOtpReq)
                .build();
        Response response = apiRequests.post(req);
        VerifyOtpResponse deserializedResponse = CommonSerializationUtil.readObject(response.getBody().asString(), VerifyOtpResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }

    public FetchOtpResponse fetchOtp(Map<String, Object> queryParams, Map<String, String> headers) {
        RestRequest req = RestRequest.builder()
                .url(BaseUri.BASE_URI + AuthServiceEndpoints.V1 + AuthServiceEndpoints.FETCH_OTP)
                .queryParams(queryParams)
                .headers(headers)
                .build();
        Response response = apiRequests.get(req);
        FetchOtpResponse deserializedResponse = CommonSerializationUtil.readObject(response.getBody().asString(), FetchOtpResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
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
