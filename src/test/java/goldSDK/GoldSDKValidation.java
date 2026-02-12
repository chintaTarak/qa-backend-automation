package goldSDK;

import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.jarApiAutomation.data.requestModel.goldSDK.CreateUserRequest;
import org.jarApiAutomation.data.responseModel.CommonResultModel;
import org.jarApiAutomation.data.responseModel.digiGold.BuyPriceResponse;
import org.jarApiAutomation.data.responseModel.goldSDK.AutoPayInitiateResponse;
import org.jarApiAutomation.data.responseModel.goldSDK.CreateUserResponse;
import org.jarApiAutomation.data.responseModel.goldSDK.GetUserResponse;
import org.jarApiAutomation.data.responseModel.goldSDK.UserAuthResponse;
import org.jarApiAutomation.utils.ApiAssertions;
import org.testng.asserts.SoftAssert;
import testData.goldSDK.GoldSDKTestData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import static org.jarApiAutomation.dbConfiguration.DBConstants.*;
import static org.jarApiAutomation.dbConfiguration.DataBaseFactory.*;
import static org.jarApiAutomation.utils.CommonUtil.getValueFromDocument;
import static testData.digiGold.DigiGoldTestData.MATERIAL_CODE;
import static testData.goldSDK.GoldSDKTestData.*;

@Slf4j
public class GoldSDKValidation extends ApiAssertions {

    public GoldSDKValidation(SoftAssert softAssert) {
        super(softAssert);
    }

    public void assertCreateUserSuccessFullResponse(
            CreateUserResponse createUserResponse, CreateUserRequest createUserRequest) {
        assertFieldNotNull(createUserResponse.getData().getId(), "id is Null");
        assertFieldsEquals(
                createUserResponse.getData().getUserRefId(),
                createUserRequest.getUserRefId(),
                "UserRefId");
        assertFieldsEquals(
                createUserResponse.getData().getCountryCode(),
                createUserRequest.getCountryCode(),
                "CountryCode");
        assertFieldsEquals(
                createUserResponse.getData().getPhoneNumber(),
                createUserRequest.getCountryCode() + createUserRequest.getPhoneNumber(),
                "PhoneNumber");
        assertFieldsEquals(
                createUserResponse.getData().getName(),
                createUserRequest.getFirstName() + " " + createUserRequest.getLastName(),
                "Name");
    }

    public void assertCreateUserErrorResponse(
            String expectedErrorCode,
            String expectedErrorMessage,
            CommonResultModel actualResponse) {
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
        assertFieldsEquals(getUserResponse.getData().getUserRefId(), GoldSDKTest.userRefId, "userRefId");
        assertFieldsEquals(
                getUserResponse.getData().getPhoneNumber(),
                USER_COUNTRY_CODE + USER_PHONE_NUMBER,
                "phoneNumber");
    }

    public void validateCreateUserInDB(
            CreateUserRequest createUserRequest, CreateUserResponse createUserResponse) {
        // Fetch the document from MongoDB
        Document doc =
                tenantMongo()
                        .fetchData(
                                TENANTS_DB,
                                TENANT_USERS_COLLECTION,
                                "_id",
                                createUserResponse.getData().getId(),
                                "_id");
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
        assertFieldsEquals(
                phoneNumber,
                createUserRequest.getCountryCode() + createUserRequest.getPhoneNumber(),
                "PhoneNumber");
    }

    public void validateUserCreation(
            CreateUserRequest createUserRequest,
            CreateUserResponse response,
            GoldSDKDataProvider.ExpectedError expectedError) {
        int expectedStatusCode =
                expectedError == null ? 200 : expectedError.getExpectedStatusCode();
        assertFieldsEquals(response.getStatusCode(), expectedStatusCode, "Status code");
        // ERROR FLOW
        if (expectedStatusCode != 200) {
            assertFieldsEquals(
                    response.getErrorCode(), expectedError.getErrorCode(), "Error code mismatch");
            String actualErrorMsg =
                    response.getErrorMessage() != null
                            ? response.getErrorMessage()
                            : response.getError();
            assertFieldTrue(
                    actualErrorMsg != null
                            && actualErrorMsg.contains(expectedError.getErrorMessage()),
                    "errorMessage",
                    "Expected error message to contain: " + expectedError.getErrorMessage()
            );
            return;
        }
        //  SUCCESS FLOW
        assertFieldTrue(response.isSuccess(), "success", "Expected success=true");
        assertCreateUserSuccessFullResponse(response, createUserRequest);
        validateCreateUserInDB(createUserRequest, response);
    }

    public void validateUserAuth(
            UserAuthResponse response, GoldSDKDataProvider.ExpectedError expectedError) {
        int expectedStatusCode =
                expectedError == null ? 200 : expectedError.getExpectedStatusCode();
        assertFieldsEquals(response.getStatusCode(), expectedStatusCode, "Status code");
        // ERROR FLOW
        if (expectedStatusCode != 200) {
            assertFieldsEquals(
                    response.getErrorCode(), expectedError.getErrorCode(), "Error code mismatch");
            String actualErrorMsg =
                    response.getErrorMessage() != null
                            ? response.getErrorMessage()
                            : response.getError();
            assertFieldTrue(
                    actualErrorMsg != null
                            && actualErrorMsg.contains(expectedError.getErrorMessage()),
                    "errorMessage",
                    "Expected error message to contain: " + expectedError.getErrorMessage()
            );
            return;
        }
        // SUCCESS FLOW
        assertFieldTrue(response.isSuccess(), "success", "Expected success=true");
        assertFieldNotNull(response.getData(), "Refresh Token response data");
        assertUserAuthResponse(response);
    }

    public void validateGetUsers(
            GetUserResponse response, GoldSDKDataProvider.ExpectedError expectedError) {
        int expectedStatusCode =
                expectedError == null ? 200 : expectedError.getExpectedStatusCode();
        assertFieldsEquals(response.getStatusCode(), expectedStatusCode, "Status code");
        // ERROR FLOW
        if (expectedStatusCode != 200) {
            assertFieldsEquals(
                    response.getErrorCode(), expectedError.getErrorCode(), "Error code mismatch");
            String actualErrorMsg =
                    response.getErrorMessage() != null
                            ? response.getErrorMessage()
                            : response.getError();
            assertFieldTrue(
                    actualErrorMsg != null
                            && actualErrorMsg.contains(expectedError.getErrorMessage()),
                    "errorMessage",
                    "Expected error message to contain: " + expectedError.getErrorMessage()
            );            return;
        }
        // SUCCESS FLOW
        assertFieldTrue(response.isSuccess(), "success", "Success flag is false");
        assertFieldNotNull(response.getData(), "Refresh Token response data");
        assertFetchUserResponse(response);
    }

    public void validateRefreshToken(
            UserAuthResponse response, GoldSDKDataProvider.ExpectedError expectedError) {
        int expectedStatusCode =
                expectedError == null ? 200 : expectedError.getExpectedStatusCode();
        assertFieldsEquals(response.getStatusCode(), expectedStatusCode, "Status code");
        // ERROR FLOW
        if (expectedStatusCode != 200) {
            assertFieldsEquals(
                    response.getErrorCode(),
                    expectedError.getErrorCode(),
                    "Error code mismatch"
            );
            String actualErrorMsg =
                    response.getErrorMessage() != null
                            ? response.getErrorMessage()
                            : response.getError();
            assertFieldTrue(
                    actualErrorMsg != null
                            && actualErrorMsg.contains(expectedError.getErrorMessage()),
                    "errorMessage",
                    "Expected error message to contain: " + expectedError.getErrorMessage()
            );
            return;
        }
        // SUCCESS FLOW
        assertFieldTrue(response.isSuccess(), "success", "Expected success=true");
        assertFieldNotNull(response.getData(), "Refresh Token response data");
        assertUserAuthResponse(response);
    }

    public void validateAutoPayInitiateResponse(AutoPayInitiateResponse response, GoldSDKDataProvider.ExpectedError expectedError) {
        int expectedStatusCode = expectedError == null ? 200 : expectedError.getExpectedStatusCode();
        assertFieldsEquals(response.getStatusCode(), expectedStatusCode, "Status code");
        AutoPayInitiateResponse.DataObj data = response.getData();
        //Positive flow validation
        if (expectedStatusCode == 200) {
            assertFieldTrue(response.isSuccess(), "success", "Expected success=true");
            assertFieldNotNull(data, "Auto Pay Initiate response data");
            assertFieldNotNull(data.getAutopayId(), "AutoPayId");
            assertFieldNotNull(data.getAutopayRefId(), "AutoPayRefId");
            assertFieldsEquals(data.getUserId(), GoldSDKTest.userId, "UserId");
            assertFieldsEquals(data.getStatus(), "PENDING", "Status");
            assertFieldNotNull(data.getNextInstallmentTime(), "NextInstallmentTime");
            assertFieldNotNull(data.getIntentUrl(), "IntentUrl");
            assertFieldsEquals(data.getAmount(), MANDATE_AMOUNT, "Amount");
            assertFieldsEquals(data.getMaxAmount(), MAX_MANDATE_AMOUNT, "Max Amount");
            assertFieldsEquals(data.getFrequency(), GoldSDKTest.frequency, "Frequency");
            validateAutoPayInitiateInDB(data);
            return;
        }

        //need to implement negative flow validations
    }

    private void validateAutoPayInitiateInDB(AutoPayInitiateResponse.DataObj data) {
        // Fetch the document from MongoDB
        Document doc = changeJarMongo().fetchData(DIGIGOLD_SDK_DB, AUTOPAYS_COLLECTION, "_id", data.getAutopayId(), "_id");

        // Extract fields from the document
        String userId = getValueFromDocument(doc, "userId");
        String autopayRefId = getValueFromDocument(doc, "sourceRefId");
        int amount = Integer.parseInt(getValueFromDocument(doc, "amount")) / 100;
        int maxAmount = Integer.parseInt(getValueFromDocument(doc, "maxAmount")) / 100;
        String status = getValueFromDocument(doc, "status");
        String paymentGateway = getValueFromDocument(doc, "paymentGateway");
        String tenantId = getValueFromDocument(doc, "tenantId");
        String autPayConfigId = getValueFromDocument(doc, "autopayConfigId");
        String pgConfigId = getValueFromDocument(doc, "pgConfigId");
        String orderId = getValueFromDocument(doc, "orderId");
        String frequency = getValueFromDocument(doc, "frequency");
        int executionDay = Integer.parseInt(getValueFromDocument(doc, "executionDay"));
        List<Document> changeLogs = doc.getList("changeLogs", Document.class);


        // Compare with response object
        assertFieldsEquals(userId, data.getUserId(), "UserId");
        assertFieldsEquals(autopayRefId, data.getAutopayRefId(), "AutopayRefId");
        assertFieldsEquals(amount, data.getAmount(), "Amount");
        assertFieldsEquals(maxAmount, data.getMaxAmount(), "MaxAmount");
        assertFieldsEquals(status, data.getStatus(), "Status");
        assertFieldsEquals(paymentGateway, "PHONEPE_V2", "PaymentGateway");
        assertFieldsEquals(frequency, data.getFrequency(), "Frequency");
        assertFieldsEquals(executionDay, 1, "ExecutionDay");
        assertFieldsEquals(tenantId, GoldSDKTestData.TENANT_ID, "TenantId");
        assertFieldNotNull(autPayConfigId, "AutopayConfigId");
        assertFieldNotNull(pgConfigId, "PgConfigId");
        assertFieldNotNull(orderId, "OrderId");
        assertFieldNotNull(changeLogs, "ChangeLogs");

    }
    public void assertSDKBuyPrice(BuyPriceResponse response,
                                  GoldSDKDataProvider.ExpectedError expectedError)
    {
        int expectedStatusCode =
                expectedError == null ? 200 : expectedError.getExpectedStatusCode();
        assertFieldsEquals(response.getStatusCode(), expectedStatusCode, "Status code");
        if (response.isSuccess()&& response.getData()!=null)
        {
            assertFieldTrue(true, "success", "Expected success=true");
            softAssert.assertNull(response.getErrorCode(), "errorCode should be null in success response");
            softAssert.assertNull(response.getErrorMessage() != null ? response.getErrorMessage() : response.getError(),
                    "error message should be null in success response");
            assertFieldNotNull(response.getData(), "Data object should not be null");
            validateSDKBuyPriceWithDB(response);
        }

    }
    private void validateSDKBuyPriceWithDB(BuyPriceResponse response) {

        if (response.getData() == null) {
            softAssert.fail("Response data is null for positive scenario");
            return;
        }

        String rateId = response.getData().getId();
        Document doc = digiGoldMongo().fetchData(DIGI_GOLD_DB, MATERIAL_RATE, id, rateId, SORT_FIELD);
        if (doc == null) {
            softAssert.fail("DB document is null for positive scenario");
            return;
        }

        String docId = doc.getObjectId("_id").toHexString();
        assertFieldsEquals(docId, rateId, "_id mismatch between DB and API");
        String dbMaterialCode = doc.getString("materialCode");
        assertFieldsEquals(dbMaterialCode, MATERIAL_CODE, "materialCode mismatch");
        String rateType = doc.getString("type");
        assertFieldsEquals(rateType, "SELL", "type in DB is not SELL");

        Object finalPriceObj = doc.get("finalPrice");

        if (!(finalPriceObj instanceof Number)) {
            softAssert.fail("Unsupported finalPrice type: " + finalPriceObj.getClass());
            return;
        }
        BigDecimal dbFinalPrice =
                BigDecimal.valueOf(((Number) finalPriceObj).longValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal apiPrice = response.getData().getAssetPrice().setScale(2, RoundingMode.HALF_UP);
        assertFieldsEquals(dbFinalPrice, apiPrice, "API assetPrice mismatch with DB finalPrice");
    }


}
