package changejar.userProfile;

import static org.jarApiAutomation.utils.CommonUtil.getApiEndPoint;

import io.restassured.response.Response;
import java.util.Map;
import org.jarApiAutomation.configuration.BaseUri;
import org.jarApiAutomation.data.common.RestRequest;
import org.jarApiAutomation.data.responseModel.userProfile.UserDetailsResponse;
import org.jarApiAutomation.endpoints.UserProfileEndpoints;
import org.jarApiAutomation.utils.ApiRequests;
import org.jarApiAutomation.utils.CommonSerializationUtil;

public class UserMethods {
    private final ApiRequests apiRequests = new ApiRequests();

    public UserDetailsResponse userDetails(Map<String, String> headers) {
        RestRequest req =
                RestRequest.builder()
                        .headers(headers)
                        .url(
                                getApiEndPoint(
                                        BaseUri.STAGING_BASE_URI,
                                        BaseUri.V1,
                                        UserProfileEndpoints.USER_DETAILS))
                        .build();
        Response response = apiRequests.get(req);
        UserDetailsResponse deserializedResponse =
                CommonSerializationUtil.readObject(
                        response.getBody().asString(), UserDetailsResponse.class);
        deserializedResponse.setStatusCode(response.getStatusCode());
        return deserializedResponse;
    }
}
