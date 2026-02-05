package changejar.autopayService;

import static org.jarApiAutomation.utils.CommonUtil.getApiEndPoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import java.util.Map;
import org.jarApiAutomation.configuration.BaseUri;
import org.jarApiAutomation.data.common.RestRequest;
import org.jarApiAutomation.data.requestModel.autopay.InitiateAutopayRequest;
import org.jarApiAutomation.data.requestModel.autopay.StatusAutopayRequest;
import org.jarApiAutomation.data.responseModel.autopay.InitiateAutopayResponse;
import org.jarApiAutomation.data.responseModel.autopay.StatusAutopayResponse;
import org.jarApiAutomation.endpoints.AutopayEndpoints;
import org.jarApiAutomation.utils.ApiRequests;
import org.jarApiAutomation.utils.CommonSerializationUtil;

public class AutopayMethods {

    private final ApiRequests apiRequests = new ApiRequests();
    private final ObjectMapper mapper = new ObjectMapper();

    // Existing method - POJO based
    public InitiateAutopayResponse initiateAutopay(
            InitiateAutopayRequest request, Map<String, String> headers) {
        RestRequest req =
                RestRequest.builder()
                        .url(
                                getApiEndPoint(
                                        BaseUri.STAGING_BASE_URI,
                                        BaseUri.V2,
                                        AutopayEndpoints.INITIATE_AUTOPAY))
                        .headers(headers)
                        .body(request)
                        .build();

        Response response = apiRequests.post(req);
        InitiateAutopayResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), InitiateAutopayResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }

    // New method - Raw JSON based
    public JsonNode initiateAutopayRaw(InitiateAutopayRequest request, String accessToken) {
        RestRequest req =
                RestRequest.builder()
                        .url(
                                getApiEndPoint(
                                        BaseUri.STAGING_BASE_URI,
                                        BaseUri.V2,
                                        AutopayEndpoints.INITIATE_AUTOPAY))
                        .headers(
                                Map.of(
                                        "Authorization",
                                        "Bearer " + accessToken,
                                        "Content-Type",
                                        "application/json"))
                        .body(request)
                        .build();

        Response response = apiRequests.post(req);
        try {
            return mapper.readTree(response.getBody().asString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse initiateAutopayRaw response as JSON", e);
        }
    }

    public StatusAutopayResponse autopayStatus(StatusAutopayRequest request, String accessToken) {

        RestRequest req =
                RestRequest.builder()
                        .url(
                                getApiEndPoint(
                                        BaseUri.STAGING_BASE_URI,
                                        BaseUri.V2,
                                        AutopayEndpoints.AUTOPAY_STATUS))
                        .headers(
                                Map.of(
                                        "Authorization",
                                        "Bearer " + accessToken,
                                        "Content-Type",
                                        "application/json"))
                        .body(request)
                        .build();

        Response response = apiRequests.post(req);

        StatusAutopayResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), StatusAutopayResponse.class);

        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }
}
