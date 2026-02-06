package digigold;

import static org.jarApiAutomation.configuration.BaseUri.DIGIGOLD_BASE_URI;
import static org.jarApiAutomation.configuration.BaseUri.V1;
import static org.jarApiAutomation.endpoints.DigiGoldEndpoints.*;
import static org.jarApiAutomation.utils.CommonUtil.getApiEndPoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.restassured.response.Response;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jarApiAutomation.configuration.BaseUri;
import org.jarApiAutomation.data.common.RestRequest;
import org.jarApiAutomation.data.requestModel.digiGold.*;
import org.jarApiAutomation.data.responseModel.digiGold.*;
import org.jarApiAutomation.data.requestModel.digiGold.BuyConfirmRequest;
import org.jarApiAutomation.data.requestModel.digiGold.BuyVerifyRequest;
import org.jarApiAutomation.data.requestModel.digiGold.CreateUserRequest;
import org.jarApiAutomation.data.requestModel.digiGold.SellConfirmRequest;
import org.jarApiAutomation.data.requestModel.digiGold.SellVerifyRequest;
import org.jarApiAutomation.data.responseModel.digiGold.*;
import org.jarApiAutomation.data.responseModel.digiGold.SellConfirmResponse;
import org.jarApiAutomation.data.responseModel.digiGold.SellPriceResponse;
import org.jarApiAutomation.data.responseModel.digiGold.SellVerifyResponse;
import org.jarApiAutomation.endpoints.DigiGoldEndpoints;
import org.jarApiAutomation.utils.ApiRequests;
import org.jarApiAutomation.utils.CommonSerializationUtil;


@Slf4j
public class DigiGoldMethods {

    private final ApiRequests apiRequests = new ApiRequests();

    public Response createUser(Map<String, String> headers, CreateUserRequest createUserReq) {
        RestRequest req = RestRequest.builder()
                .headers(headers)
                .url(getApiEndPoint(DIGIGOLD_BASE_URI, V1, DigiGoldEndpoints.CREATE_USER))
                .body(createUserReq)
                .build();
        return apiRequests.post(req);
    }

    public Response getUser(Map <String,String> headers,Map<String, Object> queryParams) {
        RestRequest req = RestRequest.builder()
                .headers(headers)
                .url(getApiEndPoint(BaseUri.DIGIGOLD_BASE_URI, BaseUri.V1, DigiGoldEndpoints.GET_USERS))
                .queryParams(queryParams)
                .build();
        return  apiRequests.get(req);
    }

    public BuyPriceResponse buyPrice(Map<String, Object> queryParams, Map<String, String> headers)
    {
        RestRequest req = RestRequest.builder()
                .url(getApiEndPoint(DIGIGOLD_BASE_URI,V1,BUY_PRICE))
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

    public SellPriceResponse getSellPrice(
            Map<String, String> headers, Map<String, String> queryParams) {
        RestRequest req =
                RestRequest.builder()
                        .url(
                                getApiEndPoint(
                                        BaseUri.DIGIGOLD_BASE_URI,
                                        BaseUri.V1,
                                        DigiGoldEndpoints.GET_SELL_PRICE))
                        .headers(headers)
                        .queryParams(queryParams)
                        .build();
        Response response = apiRequests.get(req);
        SellPriceResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), SellPriceResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }

    public SellVerifyResponse sellVerify(
            Map<String, String> headers, SellVerifyRequest sellVerifyRequest) {
        RestRequest req =
                RestRequest.builder()
                        .url(
                                getApiEndPoint(
                                        BaseUri.DIGIGOLD_BASE_URI,
                                        BaseUri.V1,
                                        DigiGoldEndpoints.SELL_VERIFY))
                        .headers(headers)
                        .body(sellVerifyRequest)
                        .build();
        Response response = apiRequests.post(req);
        SellVerifyResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), SellVerifyResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }

    public SellConfirmResponse sellConfirm(
            Map<String, String> headers, SellConfirmRequest sellConfirmRequest) {
        RestRequest req =
                RestRequest.builder()
                        .url(
                                getApiEndPoint(
                                        BaseUri.DIGIGOLD_BASE_URI,
                                        BaseUri.V1,
                                        DigiGoldEndpoints.SELL_CONFIRM))
                        .headers(headers)
                        .body(sellConfirmRequest)
                        .build();
        Response response = apiRequests.post(req);
        SellConfirmResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), SellConfirmResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }

    public SellStatusResponse sellStatus(
            Map<String, String> headers, Map<String, String> queryParam) {
        RestRequest req =
                RestRequest.builder()
                        .url(
                                getApiEndPoint(
                                        BaseUri.DIGIGOLD_BASE_URI,
                                        BaseUri.V1,
                                        DigiGoldEndpoints.SELL_STATUS))
                        .headers(headers)
                        .queryParams(queryParam)
                        .build();
        Response response = apiRequests.get(req);
        SellStatusResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), SellStatusResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }
    public InvoiceResponse invoiceDetails(Map<String, String> queryParams, Map<String, String> headers)
    {
        RestRequest req = RestRequest.builder()
                .url(getApiEndPoint(DIGIGOLD_BASE_URI,V1,INVOICE))
                .queryParams(queryParams)
                .headers(headers)
                .build();
        Response response = apiRequests.get(req);
        InvoiceResponse deserializedResponse = CommonSerializationUtil.readObject(response.getBody().asString(), InvoiceResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }


    public DeliveryOrderResponse deliveryOrder(Map <String,String> headers, DeliveryOrderRequest request)
    {
        RestRequest req = RestRequest.builder()
                .url(getApiEndPoint(BaseUri.DIGIGOLD_BASE_URI, BaseUri.V1, DigiGoldEndpoints.DELIVERY_ORDER))
                .headers(headers)
                .body(request)
                .build();
        Response response = apiRequests.post(req);
        DeliveryOrderResponse deserializedResponse = CommonSerializationUtil.readObject(response.getBody().asString(), DeliveryOrderResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }


    public DeliveryOrderConfirmResponse deliveryOrderConfirm(Map<String, String> headers, DeliveryOrderConfirmRequest request)
    {
        RestRequest req = RestRequest.builder()
                .url(getApiEndPoint(BaseUri.DIGIGOLD_BASE_URI, BaseUri.V1, DigiGoldEndpoints.DELIVERY_ORDER_CONFIRM))
                .headers(headers)
                .body(request)
                .build();
        Response response = apiRequests.post(req);
        DeliveryOrderConfirmResponse deserializedResponse = CommonSerializationUtil.readObject(response.getBody().asString(), DeliveryOrderConfirmResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }

    public DeliveryOrderResponse getOrderDeliveryDetails(Map<String, String> headers, Map<String, String> queryParams)
    {
        RestRequest req = RestRequest.builder()
                .url(getApiEndPoint(DIGIGOLD_BASE_URI,V1,DELIVERY_ORDER))
                .queryParams(queryParams)
                .headers(headers)
                .build();
        Response response = apiRequests.get(req);
        DeliveryOrderResponse deserializedResponse = CommonSerializationUtil.readObject(response.getBody().asString(), DeliveryOrderResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;

    }

    public JsonNode getAllProduct(Map<String, String> headers, Map<String, String> queryParam) {
        RestRequest req =
                RestRequest.builder()
                        .url(getApiEndPoint(DIGIGOLD_BASE_URI, V1, SEARCH_ALL_PRODUCTS))
                        .queryParams(queryParam)
                        .headers(headers)
                        .build();
        Response response = apiRequests.get(req);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(response.getBody().asString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response JSON", e);
        }
        ((ObjectNode) rootNode).put("statusCode", response.getStatusCode());
        return rootNode;
    }

    public JsonNode productDetails(Map<String, String> headers, Map<String, String> queryParam) {
        RestRequest req =
                RestRequest.builder()
                        .url(getApiEndPoint(DIGIGOLD_BASE_URI, V1, GET_PRODUCT_DETAILS))
                        .queryParams(queryParam)
                        .headers(headers)
                        .build();
        Response response = apiRequests.get(req);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(response.getBody().asString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response JSON", e);
        }
        ((ObjectNode) rootNode).put("statusCode", response.getStatusCode());
        return rootNode;
    }
}
