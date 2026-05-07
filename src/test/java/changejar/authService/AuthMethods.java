package changejar.authService;

import static org.jarApiAutomation.utils.CommonUtil.getApiEndPoint;

import io.restassured.response.Response;
import java.util.Map;
import org.jarApiAutomation.configuration.BaseUri;
import org.jarApiAutomation.data.common.RestRequest;
import org.jarApiAutomation.data.requestModel.auth.ResetOtpRequest;
import org.jarApiAutomation.data.requestModel.auth.VerifyOtpRequest;
import org.jarApiAutomation.data.responseModel.auth.FetchOtpResponse;
import org.jarApiAutomation.data.responseModel.auth.RequestOtpResponse;
import org.jarApiAutomation.data.responseModel.auth.VerifyOtpResponse;
import org.jarApiAutomation.endpoints.AuthServiceEndpoints;
import org.jarApiAutomation.utils.ApiRequests;
import org.jarApiAutomation.utils.CommonSerializationUtil;

public class AuthMethods {

  private final ApiRequests apiRequests = new ApiRequests();


public RequestOtpResponse requestOTP(Map<String, Object> phoneNumber){
    RestRequest req = RestRequest.builder().url(getApiEndPoint(BaseUri.STAGING_BASE_URI,
            BaseUri.V2,
            AuthServiceEndpoints.REQUEST_OTP)).queryParams(phoneNumber).build();
    Response response = apiRequests.post(req);
    RequestOtpResponse deserilizationResponse = CommonSerializationUtil.readObject(response.getBody().asString(),RequestOtpResponse.class);
    deserilizationResponse.setStatusCode(response.getStatusCode());
    return deserilizationResponse;

}

    public FetchOtpResponse fetchOtp(Map<String, Object> phoneNumber, Map<String, String> token){
        RestRequest req = RestRequest.builder().url(getApiEndPoint(BaseUri.STAGING_BASE_URI,
                BaseUri.V1,
                AuthServiceEndpoints.FETCH_OTP)).headers(token).queryParams(phoneNumber).build();
        Response response = apiRequests.get(req);
        FetchOtpResponse deserilizationResponse = CommonSerializationUtil.readObject(response.getBody().asString(), FetchOtpResponse.class);
        deserilizationResponse.setStatusCode(response.getStatusCode());
        return deserilizationResponse;


    }

    public VerifyOtpResponse verifyOtp(VerifyOtpRequest VerifyOtpreq){
        RestRequest req = RestRequest.builder().
                url(getApiEndPoint(BaseUri.STAGING_BASE_URI,
                BaseUri.V2,
                AuthServiceEndpoints.VERIFY_OTP)).body(VerifyOtpreq).build();
        Response response = apiRequests.post(req);
        VerifyOtpResponse deserilizationResponse = CommonSerializationUtil.readObject(response.getBody().asString(), VerifyOtpResponse.class);
        deserilizationResponse.setStatusCode(response.getStatusCode());
        return deserilizationResponse;
    }


    public Response resetOtpLimit(String countryCode, String phoneNumber) {
        ResetOtpRequest resetOtp = new ResetOtpRequest();
        resetOtp.setMobileNumber(countryCode + phoneNumber);
        RestRequest req =
                RestRequest.builder()
                        .url(
                                getApiEndPoint(
                                        BaseUri.STAGING_BASE_URI,
                                        BaseUri.V1,
                                        AuthServiceEndpoints.RESET_OTP))
                        .body(resetOtp)
                        .build();
        return apiRequests.post(req);
    }

    public Response verifyOtpLimit(String countryCode, String phoneNumber) {
        ResetOtpRequest resetOtp = new ResetOtpRequest();
        resetOtp.setMobileNumber(countryCode + phoneNumber);
        RestRequest req =
                RestRequest.builder()
                        .url(
                                getApiEndPoint(
                                        BaseUri.STAGING_BASE_URI,
                                        BaseUri.V2,
                                        AuthServiceEndpoints.RESET_VERIFY_OTP_LIMIT))
                        .body(resetOtp)
                        .build();
        return apiRequests.get(req);
    }
}
