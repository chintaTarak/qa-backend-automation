package goldSDK;

import io.restassured.response.Response;
import org.bson.Document;
import org.jarApiAutomation.data.requestModel.goldSDK.CreateUserRequest;
import org.jarApiAutomation.data.responseModel.CommonResultModel;
import org.jarApiAutomation.data.responseModel.goldSDK.CreateUserResponse;
import org.jarApiAutomation.data.responseModel.goldSDK.GetUserResponse;
import org.jarApiAutomation.data.responseModel.goldSDK.UserAuthResponse;
import org.jarApiAutomation.utils.ApiAssertions;
import org.jarApiAutomation.utils.CommonSerializationUtil;
import org.testng.asserts.SoftAssert;

import static org.jarApiAutomation.dbConfiguration.DBConstants.TENANTS_DB;
import static org.jarApiAutomation.dbConfiguration.DBConstants.TENANT_USERS_COLLECTION;
import static org.jarApiAutomation.dbConfiguration.DataBaseFactory.tenantMongo;
import static org.jarApiAutomation.utils.CommonUtil.getValueFromDocument;
import static testData.goldSDK.GoldSDKTestData.*;

public class GoldSDKValidation extends ApiAssertions {

    public GoldSDKValidation(SoftAssert softAssert) {
        super(softAssert);
    }

    public void assertCreateUserSuccessFullResponse(CreateUserResponse createUserResponse, CreateUserRequest createUserRequest) {
        assertFieldNotNull(createUserResponse.getData().getId(), "id is Null");
        assertFieldsEquals(createUserResponse.getData().getUserRefId(), createUserRequest.getUserRefId(), "UserRefId");
        assertFieldsEquals(createUserResponse.getData().getCountryCode(), createUserRequest.getCountryCode(), "CountryCode");
        assertFieldsEquals(createUserResponse.getData().getPhoneNumber(), createUserRequest.getCountryCode() + createUserRequest.getPhoneNumber(), "PhoneNumber");
        assertFieldsEquals(createUserResponse.getData().getName(), createUserRequest.getFirstName() + " " + createUserRequest.getLastName(), "Name");
    }

    public void assertCreateUserErrorResponse(String expectedErrorCode, String expectedErrorMessage, CommonResultModel actualResponse) {
        assertFieldsEquals(actualResponse.isSuccess(), false, "Success flag");
        assertFieldsEquals(actualResponse.getErrorCode(), expectedErrorCode, "Error Code");
        assertFieldsEquals(actualResponse.getMessage(), expectedErrorMessage, "Error Message");
    }

    public void assertUserAuthResponse(UserAuthResponse userAuthResponse) {
        assertFieldNotNull(userAuthResponse.getData().getAccessToken(), "accessToken");
        assertFieldNotNull(userAuthResponse.getData().getRefreshToken(), "refreshToken");
    }
    public void assertFetchUserResponse(GetUserResponse getUserResponse) {
        assertFieldsEquals(getUserResponse.getData().getUserId(), USER_ID, "userId");
        assertFieldsEquals(getUserResponse.getData().getUserRefId(), USER_REF_ID, "userRefId");
        assertFieldsEquals(getUserResponse.getData().getPhoneNumber(), USER_COUNTRY_CODE + USER_PHONE_NUMBER, "phoneNumber");
    }

    public void validateCreateUserInDB(CreateUserRequest createUserRequest, CreateUserResponse createUserResponse) {
        // Fetch the document from MongoDB
        Document doc = tenantMongo().fetchData(TENANTS_DB, TENANT_USERS_COLLECTION, "_id", createUserResponse.getData().getId(), "_id");
        // Extract fields from the document
        String firstName = getValueFromDocument(doc, "firstName");
        String lastName = getValueFromDocument(doc, "lastName");
        String countryCode = getValueFromDocument(doc, "countryCode");
        String phoneNumber = getValueFromDocument(doc, "phoneNumber");
        String tenantId = getValueFromDocument(doc, "tenantId");
        String userRefId = getValueFromDocument(doc, "userRefId");

        // Compare with request object
        assertFieldsEquals(userRefId, createUserRequest.getUserRefId(), "UserRefId");
        assertFieldsEquals(firstName, createUserRequest.getFirstName(), "FirstName");
        assertFieldsEquals(lastName, createUserRequest.getLastName(), "LastName");
        assertFieldsEquals(countryCode, createUserRequest.getCountryCode(), "CountryCode");
        assertFieldsEquals(phoneNumber, createUserRequest.getCountryCode() + createUserRequest.getPhoneNumber(), "PhoneNumber");
    }

    public void validateUserCreation(CreateUserRequest createUserRequest, CreateUserResponse response, GoldSDKDataProvider.ExpectedError expectedError) {
        int expectedStatusCode = expectedError == null ? 200 : expectedError.getExpectedStatusCode();
        assertFieldsEquals(response.getStatusCode(), expectedStatusCode, "Status code");
        // ERROR FLOW
        if (expectedStatusCode != 200) {
            softAssert.assertFalse(response.isSuccess(), "success should be false for error scenario");
            assertFieldsEquals(response.getErrorCode(), expectedError.getErrorCode(), "Error code mismatch"
            );
            String actualErrorMsg = response.getErrorMessage() != null ? response.getErrorMessage() : response.getError();

            softAssert.assertTrue(
                    actualErrorMsg != null && actualErrorMsg.contains(expectedError.getErrorMessage()),
                    "Error message mismatch"
            );
            return;
        }
        //  SUCCESS FLOW
        assertFieldTrue(response.isSuccess(), "success", "Expected success=true");
        assertCreateUserSuccessFullResponse(response, createUserRequest);
        validateCreateUserInDB(createUserRequest, response);
    }

    public void validateUserAuth(UserAuthResponse response, GoldSDKDataProvider.ExpectedError expectedError) {
        int expectedStatusCode = expectedError == null ? 200 : expectedError.getExpectedStatusCode();
        assertFieldsEquals(response.getStatusCode(), expectedStatusCode, "Status code");
        // ERROR FLOW
        if(expectedStatusCode != 200) {
            softAssert.assertFalse(response.isSuccess(), "success should be false for error scenario");
            assertFieldsEquals(response.getErrorCode(), expectedError.getErrorCode(), "Error code mismatch"
            );
            String actualErrorMsg = response.getErrorMessage() != null ? response.getErrorMessage() : response.getError();
            softAssert.assertTrue(actualErrorMsg != null && actualErrorMsg.contains(expectedError.getErrorMessage()), "Error message mismatch");
            return;
        }
        // SUCCESS FLOW
        assertFieldTrue(response.isSuccess(), "success", "Expected success=true");
        assertFieldNotNull(response.getData(), "Refresh Token response data");
        assertUserAuthResponse(response);
    }
    public void validateGetUsers(GetUserResponse response, GoldSDKDataProvider.ExpectedError expectedError) {
        int expectedStatusCode = expectedError == null ? 200 : expectedError.getExpectedStatusCode();
        assertFieldsEquals(response.getStatusCode(), expectedStatusCode, "Status code");
        // ERROR FLOW
        if (expectedStatusCode != 200) {
            softAssert.assertFalse(response.isSuccess(), "success should be false for error scenario");
            assertFieldsEquals(response.getErrorCode(), expectedError.getErrorCode(), "Error code mismatch");
            String actualErrorMsg = response.getErrorMessage() != null ? response.getErrorMessage() : response.getError();
            softAssert.assertTrue(actualErrorMsg != null && actualErrorMsg.contains(expectedError.getErrorMessage()), "Error message mismatch");
            return;
        }
        // SUCCESS FLOW
        assertFieldTrue(response.isSuccess(), "success", "Success flag is false");
        assertFieldNotNull(response.getData(), "Refresh Token response data");
        assertFetchUserResponse(response);
    }

    public void validateRefreshToken(UserAuthResponse response, GoldSDKDataProvider.ExpectedError expectedError) {
        int expectedStatusCode = expectedError == null ? 200 : expectedError.getExpectedStatusCode();
        assertFieldsEquals(response.getStatusCode(), expectedStatusCode, "Status code");
        // ERROR FLOW
        if (expectedStatusCode != 200) {
            softAssert.assertFalse(response.isSuccess(), "success should be false for error scenario");
            assertFieldsEquals(response.getErrorCode(), expectedError.getErrorCode(), "Error code mismatch");
            String actualErrorMsg = response.getErrorMessage() != null ? response.getErrorMessage() : response.getError();
            softAssert.assertTrue(actualErrorMsg != null && actualErrorMsg.contains(expectedError.getErrorMessage()), "Error message mismatch");
            return;
        }
        // SUCCESS FLOW
        assertFieldTrue(response.isSuccess(), "success", "Expected success=true");
        assertFieldNotNull(response.getData(), "Refresh Token response data");
        assertUserAuthResponse(response);
    }
}
