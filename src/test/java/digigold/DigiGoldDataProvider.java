package digigold;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jarApiAutomation.utils.CommonUtil;
import org.testng.annotations.DataProvider;
import java.util.Map;

import static digigold.DigiGoldDataProvider.ExpectedError.*;
import static org.jarApiAutomation.data.requestModel.digiGold.CreateUserRequest.createUser;
import static testData.digiGold.DigiGoldTestData.*;


public class DigiGoldDataProvider {


    @AllArgsConstructor
    @Getter
    enum ExpectedError {

        ACCESS_DENIED("403", "Access Denied", 400),
        FORBIDDEN(null, null, 403),
        INVALID_USER_REF("20023", "User ref id or phone number is required", 400),
        USER_NOT_EXIST("20014", "User does not exist", 400),
        QUERY_PARAM_MISSING("10001", "Required query parameter userId is not present", 400);
        private final String errorCode;
        private final String errorMessage;
        private final int expectedStatusCode;
    }


    @DataProvider(name = "userCreationScenarios")
    public Object[][] userDetails() {
        String userRefId = CommonUtil.generateMongoId();
        return new Object[][]{
                {createUser(USER_FIRST_NAME, USER_LAST_NAME, USER_PHONE_NUMBER, USER_COUNTRY_CODE, userRefId), X_TENANT_INFO, 200, null, null},
                {createUser("TestUsers", USER_LAST_NAME, USER_PHONE_NUMBER, USER_COUNTRY_CODE, userRefId), X_TENANT_INFO, 200, null, null},
                {createUser(USER_FIRST_NAME, USER_LAST_NAME, USER_PHONE_NUMBER, USER_COUNTRY_CODE, userRefId), "Without-Security-Header", ACCESS_DENIED.getExpectedStatusCode(), ACCESS_DENIED.getErrorCode(), ACCESS_DENIED.getErrorMessage()},
                {createUser(USER_FIRST_NAME, USER_LAST_NAME, USER_PHONE_NUMBER, USER_COUNTRY_CODE, userRefId), "", ACCESS_DENIED.getExpectedStatusCode(), ACCESS_DENIED.getErrorCode(), ACCESS_DENIED.getErrorMessage()},
                {createUser(USER_FIRST_NAME, USER_LAST_NAME, USER_PHONE_NUMBER, USER_COUNTRY_CODE, userRefId), "wrong-x-tenant-info", FORBIDDEN.getExpectedStatusCode(), FORBIDDEN.getErrorCode(), FORBIDDEN.getErrorMessage()},
                {createUser(USER_FIRST_NAME, USER_LAST_NAME, "", USER_COUNTRY_CODE, ""), X_TENANT_INFO, INVALID_USER_REF.getExpectedStatusCode(), INVALID_USER_REF.getErrorCode(), INVALID_USER_REF.getErrorMessage()},
        };
    }


    @DataProvider(name = "getUserScenarios")
    public Object[][] getUserDetails() {

        return new Object[][]{
                // Valid request with correct X-Tenant-Info and valid User ID
                {X_TENANT_INFO, Map.of("userId",USER_ID),200, null, null},
                // Request without Security Header → Access should be denied
                {"Without-Security-Header", Map.of("userId",USER_ID),ACCESS_DENIED.getExpectedStatusCode(), ACCESS_DENIED.getErrorCode(), ACCESS_DENIED.getErrorMessage()},
                // Request with incorrect X-Tenant-Info → Forbidden access
                {"wrong-x-tenant-info", Map.of("userId",USER_ID), FORBIDDEN.getExpectedStatusCode(), FORBIDDEN.getErrorCode(), FORBIDDEN.getErrorMessage()},
                // Request with invalid User ID → User does not exist
                {X_TENANT_INFO, Map.of("userId","wrong-user-id"),USER_NOT_EXIST.getExpectedStatusCode(), USER_NOT_EXIST.getErrorCode(), USER_NOT_EXIST.getErrorMessage()},
                // Request with missing query parameters → Query parameter validation failure
                {X_TENANT_INFO, null,QUERY_PARAM_MISSING.getExpectedStatusCode(), QUERY_PARAM_MISSING.getErrorCode(), QUERY_PARAM_MISSING.getErrorMessage()},
        };
    }
}
