package digigold;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jarApiAutomation.utils.CommonUtil;
import org.testng.annotations.DataProvider;

import static digigold.DigiGoldDataProvider.ExpectedError.*;
import static org.jarApiAutomation.data.requestModel.digiGold.CreateUserRequest.createUser;
import static testData.digiGold.DigiGoldTestData.*;


public class DigiGoldDataProvider {


    @AllArgsConstructor
    @Getter
    enum ExpectedError {

        ACCESS_DENIED("403", "Access Denied", 400),
        FORBIDDEN(null, null, 403),
        INVALID_USER_REF("20023", "User ref id or phone number is required", 400);
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
}
