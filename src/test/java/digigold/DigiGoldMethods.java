package digigold;

import io.restassured.response.Response;
import org.jarApiAutomation.configuration.BaseUri;
import org.jarApiAutomation.data.common.RestRequest;
import org.jarApiAutomation.data.requestModel.digiGold.CreateUserRequest;
import org.jarApiAutomation.data.responseModel.digiGold.UserResponse;
import org.jarApiAutomation.endpoints.DigiGoldEndpoints;
import org.jarApiAutomation.utils.ApiRequests;
import org.jarApiAutomation.utils.CommonSerializationUtil;

import java.util.Map;
import static org.jarApiAutomation.utils.CommonUtil.getApiEndPoint;

public class DigiGoldMethods {

    private final ApiRequests apiRequests = new ApiRequests();

    public Response createUser(Map<String, String> headers, CreateUserRequest createUserReq) {
        RestRequest req = RestRequest.builder()
                .headers(headers)
                .url(getApiEndPoint(BaseUri.DIGIGOLD_BASE_URI, BaseUri.V1, DigiGoldEndpoints.CREATE_USER))
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

}
