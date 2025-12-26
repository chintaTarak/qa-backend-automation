package digigold;

import static org.jarApiAutomation.configuration.BaseUri.DIGIGOLD_BASE_URI;
import static org.jarApiAutomation.configuration.BaseUri.V1;
import static org.jarApiAutomation.endpoints.DigiGoldEndpoints.*;
import static org.jarApiAutomation.utils.CommonUtil.getApiEndPoint;

import io.restassured.response.Response;
import java.util.Map;
import org.jarApiAutomation.configuration.BaseUri;
import org.jarApiAutomation.data.common.RestRequest;
import org.jarApiAutomation.data.requestModel.digiGold.BuyConfirmRequest;
import org.jarApiAutomation.data.requestModel.digiGold.BuyVerifyRequest;
import org.jarApiAutomation.data.requestModel.digiGold.CreateUserRequest;
import org.jarApiAutomation.data.responseModel.digiGold.*;
import org.jarApiAutomation.endpoints.DigiGoldEndpoints;
import org.jarApiAutomation.utils.ApiRequests;
import org.jarApiAutomation.utils.CommonSerializationUtil;

public class DigiGoldMethods {

    private final ApiRequests apiRequests = new ApiRequests();

    public Response createUser(Map<String, String> headers, CreateUserRequest createUserReq) {
        RestRequest req =
                RestRequest.builder()
                        .headers(headers)
                        .url(getApiEndPoint(DIGIGOLD_BASE_URI, V1, DigiGoldEndpoints.CREATE_USER))
                        .body(createUserReq)
                        .build();
        return apiRequests.post(req);
    }

    public Response getUser(Map<String, String> headers, Map<String, Object> queryParams) {
        RestRequest req =
                RestRequest.builder()
                        .headers(headers)
                        .url(
                                getApiEndPoint(
                                        BaseUri.DIGIGOLD_BASE_URI,
                                        BaseUri.V1,
                                        DigiGoldEndpoints.GET_USERS))
                        .queryParams(queryParams)
                        .build();
        return apiRequests.get(req);
    }

    public BuyPriceResponse buyPrice(Map<String, Object> queryParams, Map<String, String> headers) {
        RestRequest req =
                RestRequest.builder()
                        .url(getApiEndPoint(DIGIGOLD_BASE_URI, V1, BUY_PRICE))
                        .queryParams(queryParams)
                        .headers(headers)
                        .build();
        Response response = apiRequests.get(req);
        BuyPriceResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), BuyPriceResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }

    public BuyVerifyResponse buyVerify(
            Map<String, String> headers, BuyVerifyRequest buyVerifyRequest) {
        RestRequest req =
                RestRequest.builder()
                        .url(getApiEndPoint(DIGIGOLD_BASE_URI, V1, BUY_VERIFY))
                        .headers(headers)
                        .body(buyVerifyRequest)
                        .build();
        Response response = apiRequests.post(req);
        BuyVerifyResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), BuyVerifyResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }

    public BuyConfirmResponse buyConfirm(
            Map<String, String> headers, BuyConfirmRequest buyConfirmRequest) {
        RestRequest req =
                RestRequest.builder()
                        .url(getApiEndPoint(DIGIGOLD_BASE_URI, V1, BUY_CONFIRM))
                        .headers(headers)
                        .body(buyConfirmRequest)
                        .build();
        Response response = apiRequests.post(req);
        BuyConfirmResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), BuyConfirmResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }

    public BuyStatusResponse buyStatus(
            Map<String, String> queryParams, Map<String, String> headers) {
        RestRequest req =
                RestRequest.builder()
                        .url(getApiEndPoint(DIGIGOLD_BASE_URI, V1, BUY_STATUS))
                        .queryParams(queryParams)
                        .headers(headers)
                        .build();
        Response response = apiRequests.get(req);
        BuyStatusResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), BuyStatusResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }
}
