package digigold;

import io.restassured.response.Response;
import org.bson.Document;
import org.jarApiAutomation.data.requestModel.digiGold.CreateUserRequest;
import org.jarApiAutomation.data.responseModel.digiGold.DigiGoldCommonErrorResponse;
import org.jarApiAutomation.data.responseModel.digiGold.UserResponse;
import org.jarApiAutomation.utils.ApiAssertions;
import org.jarApiAutomation.utils.CommonSerializationUtil;
import org.testng.asserts.SoftAssert;

import static org.jarApiAutomation.dbConfiguration.DataBaseFactory.tenantMongo;
import static org.jarApiAutomation.utils.CommonUtil.getValueFromDocument;
import static testData.digiGold.DigiGoldTestData.*;

public class DigiGoldValidation extends ApiAssertions {

    private static final String mongoIdRegEx = "^[a-fA-F0-9]{24}$";

    public DigiGoldValidation(SoftAssert softAssert) {
        super(softAssert);
    }

    public void assertCreateUserSuccessFullResponse(UserResponse createUserResponse, CreateUserRequest createUserRequest) {
        assertFieldNotNull(createUserResponse.getData().getId(), "mongoId is Null");
        assertFieldsEquals(createUserResponse.getData().getUserRefId(), createUserRequest.getUserRefId(), "UserRefId");
        assertFieldsEquals(createUserResponse.getData().getCountryCode(), createUserRequest.getCountryCode(), "CountryCode");
        assertFieldsEquals(createUserResponse.getData().getPhoneNumber(), createUserRequest.getCountryCode() + createUserRequest.getPhoneNumber(), "PhoneNumber");
        assertFieldsEquals(createUserResponse.getData().getName(), createUserRequest.getFirstName() + " " + createUserRequest.getLastName(), "Name");
        assertFieldTrue(createUserResponse.getData().getId().matches(mongoIdRegEx), "Id", "Id does not match MongoDB ID format");
    }

    public void assertCreateUserErrorResponse(String expectedErrorCode, String expectedErrorMessage, DigiGoldCommonErrorResponse actualResponse) {
        assertFieldsEquals(actualResponse.getSuccess(), false, "Success flag");
        assertFieldsEquals(actualResponse.getErrorCode(), expectedErrorCode, "Error Code");
        assertFieldsEquals(actualResponse.getError(), expectedErrorMessage, "Error Message");
    }


    public void validateCreateUserInDB(CreateUserRequest createUserRequest, UserResponse createUserResponse) {
        // Fetch the document from MongoDB
        Document doc = tenantMongo().fetchData("tenants", "tenantUsers", "_id", createUserResponse.getData().getId(), "_id");
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
        assertFieldsEquals(tenantId, TENANT_ID, "TenantId");
    }

    public void validateUserCreation(int expectedStatusCode, String X_TENANT_INFO, CreateUserRequest createUserRequest, Response createUserResponse,
                                     String expectedErrorCode, String expectedErrorMessage) {

        int actualStatusCode = createUserResponse.getStatusCode();
        assertFieldsEquals(actualStatusCode, expectedStatusCode, "Status code");

        // Response body assertion is skipped since the API returns an empty body when X-Tenant-Info is invalid
        if ("wrong-x-tenant-info".equalsIgnoreCase(X_TENANT_INFO)) {
            softAssert.assertAll();
            return;
        }

        if (expectedStatusCode == 200) {
            UserResponse validUserResponse = CommonSerializationUtil.readObject(createUserResponse.getBody().asString(), UserResponse.class);
            // Validate successful response body
            createUserRequest.setFirstName(USER_FIRST_NAME);
            createUserRequest.setLastName(USER_LAST_NAME);
            assertCreateUserSuccessFullResponse(validUserResponse, createUserRequest);
            // Validate in DB
            validateCreateUserInDB(createUserRequest, validUserResponse);
        } else {
            DigiGoldCommonErrorResponse errorResponse = CommonSerializationUtil.readObject(createUserResponse.getBody().asString(), DigiGoldCommonErrorResponse.class);
            // Validate error response body
            assertCreateUserErrorResponse(expectedErrorCode, expectedErrorMessage, errorResponse);
        }
    }
}
