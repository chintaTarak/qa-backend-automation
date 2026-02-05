package activation.manualBuyGold;

import static changejar.authService.AuthServiceTest.accessToken;
import static org.jarApiAutomation.configuration.BaseUri.*;
import static org.jarApiAutomation.endpoints.BuyGoldEndPoints.*;
import static org.jarApiAutomation.utils.CommonUtil.getApiEndPoint;

import io.restassured.response.Response;
import java.util.Map;

import org.jarApiAutomation.data.common.RestRequest;
import org.jarApiAutomation.data.requestModel.activation.BuyGoldManualRequest;
import org.jarApiAutomation.data.requestModel.activation.PaymentStatusRequest;
import org.jarApiAutomation.data.responseModel.auth.responseBuyGold.BuyGoldResponse;
import org.jarApiAutomation.data.responseModel.auth.responseBuyGold.GetLivePriceResponse;
import org.jarApiAutomation.data.responseModel.auth.responseBuyGold.PaymentStatusResponse;
import org.jarApiAutomation.utils.ApiRequests;
import org.jarApiAutomation.utils.CommonSerializationUtil;

public class BuyGoldMethods {

    private static final ApiRequests apiRequests = new ApiRequests();

    public static GetLivePriceResponse getLiveGoldPrice(
            Map<String, String> headers, Map<String, String> queryParams) {
        RestRequest req =
                RestRequest.builder()
                        .headers(headers)
                        .url(getApiEndPoint(STAGING_BASE_URI, V2, GETLIVEGOLDPRICE))
                        .queryParams(queryParams)
                        .build();
        Response response = apiRequests.get(req);
        return CommonSerializationUtil.readObject(
                response.getBody().asString(), GetLivePriceResponse.class);
    }

    public static BuyGoldResponse postBuyGoldManual(
            BuyGoldManualRequest buyGoldRequest,  Map<String, String> headers) {

        RestRequest req =
                RestRequest.builder()
                        .headers(headers)
                        .url(getApiEndPoint(STAGING_BASE_URI, V3, GOLDBUYMANUAL))
                        .body(buyGoldRequest)
                        .build();
        Response response = apiRequests.post(req);
        BuyGoldResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), BuyGoldResponse.class);
        return deserializedResponse;
    }

    public static PaymentStatusResponse postPaymentStatus(
            PaymentStatusRequest paymentRequest, String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .headers(
                                Map.of(
                                        "content-type",
                                        "application/json",
                                        "Authorization",
                                        "Bearer " + accessToken))
                        .url(getApiEndPoint(STAGING_BASE_URI, V2,PAYMENTSTATUS))
                        .body(paymentRequest)
                        .build();
        Response response = apiRequests.post(req);
        PaymentStatusResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), PaymentStatusResponse.class);
        return deserializedResponse;
    }
}
