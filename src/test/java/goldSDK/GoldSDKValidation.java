package goldSDK;

import io.restassured.response.Response;
import org.bson.Document;
import org.jarApiAutomation.data.requestModel.goldSDK.CreateUserRequest;
import org.jarApiAutomation.data.requestModel.goldSDK.InitiateKycRequest;
import org.jarApiAutomation.data.responseModel.CommonResultModel;
import org.jarApiAutomation.data.responseModel.goldSDK.*;
import org.jarApiAutomation.utils.ApiAssertions;
import org.jarApiAutomation.utils.CommonSerializationUtil;
import org.testng.asserts.SoftAssert;

import java.util.List;

import static org.jarApiAutomation.dbConfiguration.DBConstants.*;
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
        assertFieldsEquals(getUserResponse.getData().getUserId(), GoldSDKTest.userId, "userId");
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
    public void validateKycInDB(String userId, String expectedDocFrontId, String expectedName, String expectedKycStatus) {
        // Fetch latest KYC document for user
        Document doc = tenantMongo().fetchData(TENANTS_DB, TENANT_KYC_COLLECTION, "userId", userId, "createdAt");
        softAssert.assertNotNull(doc, "KYC document not found in DB for userId: " + userId);
        // Extract DB values
        String kycDocType = getValueFromDocument(doc, "kycDocType");
        String verificationStatus = getValueFromDocument(doc, "verificationStatus");
        String name = getValueFromDocument(doc, "name");
        String docFrontId = getValueFromDocument(doc, "docFrontId");
        Boolean isActive = doc.getBoolean("isActive");
        // Assertions
        assertFieldsEquals(kycDocType, "PAN", "KYC Doc Type");
        assertFieldsEquals(verificationStatus, expectedKycStatus, "Verification Status");
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
            softAssert.assertTrue(actualErrorMsg != null && actualErrorMsg.contains(expectedError.getErrorMessage()), "Error message mismatch"
            );
            return;
        }
        //  SUCCESS FLOW
        assertFieldTrue(response.isSuccess(), "success", "Expected success=true");
        assertCreateUserSuccessFullResponse(response, createUserRequest);
        GoldSDKTest.userId = response.getData().getId();
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
    public void validateUpload(UploadResponse response) {

        assertFieldsEquals(response.getStatusCode(), 200, "Status code");
        assertFieldTrue(response.isSuccess(), "success", "Expected success=true");

        assertFieldNotNull(response.getData(), "data");
        assertFieldNotNull(response.getData().getPreSignedUrlPath(), "preSignedUrlPath");
        assertFieldNotNull(response.getData().getDocumentImageId(), "documentImageId");
    }
    public void validateUploadFile(int statusCode) {
        assertFieldsEquals(statusCode, 200, "Upload file status code");
    }
    public void validateInitiateKyc(InitiateKycResponse response,  InitiateKycRequest request, String documentImageId, GoldSDKDataProvider.ExpectedError expectedError) {
        int expectedStatusCode = expectedError == null ? 200 : expectedError.getExpectedStatusCode();
        assertFieldsEquals(response.getStatusCode(), expectedStatusCode, "Status code");
        //  ERROR FLOW
        if (expectedStatusCode != 200) {
            softAssert.assertFalse(response.isSuccess(), "success should be false for error scenario");
            assertFieldsEquals(response.getErrorCode(), expectedError.getErrorCode(), "Error code mismatch");
            String actualMsg = response.getErrorMessage() != null ? response.getErrorMessage() : response.getError();
            softAssert.assertTrue(actualMsg != null && actualMsg.contains(expectedError.getErrorMessage()), "Error message mismatch");
            return;
        }

        // SUCCESS FLOW
        assertFieldTrue(response.isSuccess(), "success", "Expected success=true");
        assertFieldNotNull(response.getData(), "data");
        InitiateKycResponse.DataObj data = response.getData();
        assertFieldNotNull(data.getUserId(), "userId");
        assertFieldsEquals(data.getVerificationStatus(), "PENDING", "verificationStatus");
        validateKycInDB(response.getData().getUserId(), documentImageId, request.getPanVerificationDoc().getName(), "PENDING");
    }
    public void validateKycStatus(KycStatusResponse response, GoldSDKDataProvider.ExpectedError expectedError) {
        int expectedStatusCode = expectedError == null ? 200 : expectedError.getExpectedStatusCode();
        assertFieldsEquals(response.getStatusCode(), expectedStatusCode, "Status code");
        // ERROR FLOW
        if (expectedError != null) {
            softAssert.assertFalse(response.isSuccess(), "success should be false");
            assertFieldsEquals(response.getErrorCode(), expectedError.getErrorCode(), "Error code");
            return;
        }
        // SUCCESS FLOW
        assertFieldTrue(response.isSuccess(), "success", "Expected success=true");
        assertFieldsEquals(response.getData().isKycVerified(), "true", "Status code");
        List<KycStatusResponse.KycDocumentDetails> docs = response.getData().getKycDocumentDetails();
        assertFieldsEquals(docs.get(0).getVerificationStatus(), "SUCCESS", "Verification Status");
        assertFieldNotNull(response.getData(), "KYC data");
        softAssert.assertNotNull(response.getData().getUserId(), "userId");
    }
}
