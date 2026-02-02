package goldSDK;

import io.restassured.response.Response;
import org.jarApiAutomation.configuration.BaseUri;
import org.jarApiAutomation.data.common.RestRequest;
import org.jarApiAutomation.data.requestModel.goldSDK.AutoPayInitiateRequest;
import org.jarApiAutomation.data.requestModel.goldSDK.CreateUserRequest;
import org.jarApiAutomation.data.requestModel.goldSDK.RefreshTokenRequest;
import org.jarApiAutomation.data.responseModel.goldSDK.AutoPayInitiateResponse;
import org.jarApiAutomation.data.responseModel.goldSDK.CreateUserResponse;
import org.jarApiAutomation.data.responseModel.goldSDK.GetUserResponse;
import org.jarApiAutomation.data.responseModel.goldSDK.UserAuthResponse;
import org.jarApiAutomation.endpoints.GoldSDKEndPoints;
import org.jarApiAutomation.utils.ApiRequests;
import org.jarApiAutomation.utils.CommonSerializationUtil;

import java.util.Map;

import static org.jarApiAutomation.utils.CommonUtil.getApiEndPoint;

public class GoldSDKMethods {

    private final ApiRequests apiRequests = new ApiRequests();

    public CreateUserResponse createUser(
            Map<String, String> headers, CreateUserRequest createUserReq) {
        RestRequest req =
                RestRequest.builder()
                        .headers(headers)
                        .url(
                                getApiEndPoint(
                                        BaseUri.GOLD_BASE_URI, BaseUri.V1, GoldSDKEndPoints.USERS))
                        .body(createUserReq)
                        .build();
        Response response = apiRequests.post(req);
        CreateUserResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), CreateUserResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }

    public UserAuthResponse userAuth(Map<String, String> headers, Map<String, Object> queryParams) {
        RestRequest req =
                RestRequest.builder()
                        .headers(headers)
                        .url(
                                getApiEndPoint(
                                        BaseUri.GOLD_BASE_URI,
                                        BaseUri.V1,
                                        GoldSDKEndPoints.USER_AUTH))
                        .queryParams(queryParams)
                        .build();
        Response response = apiRequests.post(req);
        UserAuthResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), UserAuthResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }

    public GetUserResponse getUser(Map<String, String> headers, Map<String, Object> queryParams) {
        RestRequest req =
                RestRequest.builder()
                        .headers(headers)
                        .url(
                                getApiEndPoint(
                                        BaseUri.GOLD_BASE_AUTH_URI,
                                        BaseUri.V1,
                                        GoldSDKEndPoints.USERS))
                        .queryParams(queryParams)
                        .build();
        Response response = apiRequests.get(req);
        GetUserResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), GetUserResponse.class);
        // IMPORTANT
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }

    public UserAuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        RestRequest req =
                RestRequest.builder()
                        .url(
                                getApiEndPoint(
                                        BaseUri.GOLD_BASE_REFRESH_TOKEN_URI,
                                        BaseUri.V1,
                                        GoldSDKEndPoints.REFRESH_TOKEN))
                        .body(refreshTokenRequest)
                        .build();
        Response response = apiRequests.post(req);
        UserAuthResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), UserAuthResponse.class);

        // IMPORTANT
        deserializedResponse.setStatusCode(response.getStatusCode());

        return deserializedResponse;
    }


    public AutoPayInitiateResponse initiateAutoPay(AutoPayInitiateRequest autoPayInitiateRequest,Map<String,String> headers) {
        RestRequest req =
                RestRequest.builder()
                        .url(
                                getApiEndPoint(
                                        BaseUri.GOLD_BASE_AUTH_URI,
                                        BaseUri.V1,
                                        GoldSDKEndPoints.INITIATE_AUTOPAY))
                        .body(autoPayInitiateRequest).headers(headers)
                        .build();
        Response response = apiRequests.post(req);
        AutoPayInitiateResponse deserializedResponse = CommonSerializationUtil.readObject(response.getBody().asString(), AutoPayInitiateResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }
}
