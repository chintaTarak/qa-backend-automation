package digigold;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.jarApiAutomation.data.requestModel.digiGold.CreateUserRequest;
import org.jarApiAutomation.data.responseModel.digiGold.*;
import org.jarApiAutomation.dbConfiguration.DataBaseFactory;
import org.jarApiAutomation.utils.ApiAssertions;
import org.jarApiAutomation.utils.CommonSerializationUtil;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import static digigold.DigiGoldDataProvider.ExpectedError;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.jarApiAutomation.dbConfiguration.DataBaseFactory.*;
import static org.jarApiAutomation.utils.CommonUtil.getValueFromDocument;
import static testData.digiGold.DigiGoldTestData.*;

@Slf4j
public class DigiGoldValidation extends ApiAssertions {

    private static final String mongoIdRegEx = "^[a-fA-F0-9]{24}$";
    private static final String WRONG_TENANT_INFO = "wrong-x-tenant-info";

    public DigiGoldValidation(SoftAssert softAssert) {
        super(softAssert);
    }

    public void assertCreateUserSuccessFullResponse(
            UserResponse createUserResponse, CreateUserRequest createUserRequest) {
        assertFieldNotNull(createUserResponse.getData().getId(), "mongoId is Null");
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
        assertFieldTrue(
                createUserResponse.getData().getId().matches(mongoIdRegEx),
                "Id",
                "Id does not match MongoDB ID format");
    }

    public void assertCreateUserErrorResponse(
            String expectedErrorCode,
            String expectedErrorMessage,
            DigiGoldCommonErrorResponse actualResponse) {
        assertFieldsEquals(actualResponse.getSuccess(), false, "Success flag");
        assertFieldsEquals(actualResponse.getErrorCode(), expectedErrorCode, "Error Code");
        assertFieldsEquals(actualResponse.getError(), expectedErrorMessage, "Error Message");
    }

    public void validateCreateUserInDB(
            CreateUserRequest createUserRequest, UserResponse createUserResponse) {
        // Fetch the document from MongoDB
        Document doc =
                tenantMongo()
                        .fetchData(
                                "tenants",
                                "tenantUsers",
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
        assertFieldsEquals(tenantId, TENANT_ID, "TenantId");
    }

    public void validateUserCreation(
            int expectedStatusCode,
            String X_TENANT_INFO,
            CreateUserRequest createUserRequest,
            Response createUserResponse,
            String expectedErrorCode,
            String expectedErrorMessage) {

        int actualStatusCode = createUserResponse.getStatusCode();
        assertFieldsEquals(actualStatusCode, expectedStatusCode, "Status code");

        // Response body assertion is skipped since the API returns an empty body when X-Tenant-Info
        // is invalid
        if (WRONG_TENANT_INFO.equalsIgnoreCase(X_TENANT_INFO)) {
            softAssert.assertAll();
            return;
        }

        if (expectedStatusCode == 200) {
            UserResponse validUserResponse =
                    CommonSerializationUtil.readObject(
                            createUserResponse.getBody().asString(), UserResponse.class);
            // Validate successful response body
            createUserRequest.setFirstName(USER_FIRST_NAME);
            createUserRequest.setLastName(USER_LAST_NAME);
            assertCreateUserSuccessFullResponse(validUserResponse, createUserRequest);
            // Validate in DB
            validateCreateUserInDB(createUserRequest, validUserResponse);
        } else {
            DigiGoldCommonErrorResponse errorResponse =
                    CommonSerializationUtil.readObject(
                            createUserResponse.getBody().asString(),
                            DigiGoldCommonErrorResponse.class);
            // Validate error response body
            assertCreateUserErrorResponse(expectedErrorCode, expectedErrorMessage, errorResponse);
        }
    }

    public void validateGetUsers(
            Response getUserResponse,
            String X_TENANT_INFO,
            int expectedStatusCode,
            String expectedErrorCode,
            String expectedErrorMessage) {

        int actualStatusCode = getUserResponse.getStatusCode();
        assertFieldsEquals(actualStatusCode, expectedStatusCode, "Status code");

        // Response body assertion is skipped since the API returns an empty body when X-Tenant-Info
        // is invalid
        if (WRONG_TENANT_INFO.equalsIgnoreCase(X_TENANT_INFO)) {
            softAssert.assertAll();
            return;
        }

        if (expectedStatusCode == 200) {
            UserResponse validUserResponse =
                    CommonSerializationUtil.readObject(
                            getUserResponse.getBody().asString(), UserResponse.class);
            // Validate successful response body
            assertFieldTrue(validUserResponse.isSuccess(), "success", "Success flag is not true");
            assertFieldsEquals(validUserResponse.getData().getId(), USER_ID, "id");
            assertFieldsEquals(
                    validUserResponse.getData().getUserRefId(), USER_REF_ID, "userRefId");
            assertFieldsEquals(
                    validUserResponse.getData().getPhoneNumber(),
                    USER_COUNTRY_CODE + USER_PHONE_NUMBER,
                    "phoneNumber");
            assertFieldsEquals(
                    validUserResponse.getData().getCountryCode(), USER_COUNTRY_CODE, "countryCode");
            assertFieldsEquals(
                    validUserResponse.getData().getName(),
                    USER_FIRST_NAME + " " + USER_LAST_NAME,
                    "name");
            assertFieldsEquals(
                    validUserResponse.getData().getCurrentBalance(),
                    USER_CURRENT_BALANCE,
                    "currentBalance");

        } else {
            DigiGoldCommonErrorResponse errorResponse =
                    CommonSerializationUtil.readObject(
                            getUserResponse.getBody().asString(),
                            DigiGoldCommonErrorResponse.class);
            // Validate error response body
            assertCreateUserErrorResponse(expectedErrorCode, expectedErrorMessage, errorResponse);
        }
    }

    public void assertBuyPriceResponse(
            Document doc,
            BuyPriceResponse response,
            int expectedStatusCode,
            String expectedErrorCode,
            String expectedErrorMessage) {

        assertFieldsEquals(response.getStatusCode(), expectedStatusCode, "Status code mismatch");
        if (expectedErrorCode != null || expectedErrorMessage != null) {
            assertFieldFalse(response.isSuccess(), "success", "Expected success=false");
            if (expectedErrorCode != null) {
                assertFieldsEquals(
                        response.getErrorCode(), expectedErrorCode, "Error Code mismatch");
            }

            if (expectedErrorMessage != null) {
                String actualErrorMessage =
                        response.getError() != null
                                ? response.getError()
                                : response.getErrorMessage();
                assertFieldNotNull(actualErrorMessage, "error/errorMessage");
                assertFieldTrue(
                        actualErrorMessage.contains(expectedErrorMessage),
                        "error",
                        "Error message mismatch");
            }
            return;
        }

        if (doc == null) {
            softAssert.fail("DB document is null for positive scenario");
            return;
        }

        String docId = doc.getObjectId("_id").toHexString();
        String apiId = response.getData().getId();
        assertFieldsEquals(docId, apiId, "_id mismatch between DB and API");

        String dbMaterialCode = doc.getString("materialCode");
        assertFieldsEquals(dbMaterialCode, MATERIAL_CODE, "materialCode mismatch");

        String rateType = doc.getString("type");
        assertFieldsEquals(rateType, "SELL", "type in DB is not SELL");

        Object finalPriceObj = doc.get("finalPrice");
        BigDecimal finalPrice;

        if (finalPriceObj instanceof Number) {
            finalPrice =
                    BigDecimal.valueOf(((Number) finalPriceObj).longValue())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            softAssert.fail("Unsupported finalPrice type: " + finalPriceObj.getClass());
            return;
        }

        BigDecimal apiPrice = response.getData().getAssetPrice().setScale(2, RoundingMode.HALF_UP);
        assertFieldsEquals(finalPrice, apiPrice, "API assetPrice mismatch with DB finalPrice");
    }

    public void assertBuyVerifyResponse(
            BuyVerifyResponse response,
            int expectedStatusCode,
            String expectedErrorCode,
            String expectedErrorMessage) {
        assertFieldsEquals(response.getStatusCode(), expectedStatusCode, "Status code mismatch");

        if (expectedErrorCode != null || expectedErrorMessage != null) {
            if (!response.isSuccess()) {
                softAssert.assertFalse(
                        response.isSuccess(), "success should be false for error scenario");
            }
            if (expectedErrorCode != null) {
                assertFieldsEquals(
                        response.getErrorCode(), expectedErrorCode, "Error code mismatch");
            }
            if (expectedErrorMessage != null) {
                String actualErrorMsg =
                        response.getErrorMessage() != null
                                ? response.getErrorMessage()
                                : response.getError();
                softAssert.assertTrue(
                        actualErrorMsg != null && actualErrorMsg.contains(expectedErrorMessage),
                        "Error message mismatch. Expected to contain: "
                                + expectedErrorMessage
                                + ", but was: "
                                + actualErrorMsg);
            }
            return;
        }

        assertFieldTrue(response.isSuccess(), "success", "Expected success=true");

        if (response.getData() != null) {
            BuyVerifyResponse.DataObj data = response.getData();

            assertFieldNotNull(data.getOrderId(), "orderId");
            assertFieldNotNull(data.getVolume(), "volume");
        } else {
            softAssert.fail("Data is null in success response");
        }
    }

    public void assertBuyConfirm(BuyConfirmResponse response, ExpectedError expectedError) {
        assertFieldNotNull(response, "Response cannot be null");
        assertFieldNotNull(response.getErrorCode(), "errorCode");
        assertFieldNotNull(response.getError(), "error");
        if (response.getStatusCode() == 200) {
            BuyConfirmResponse.DataResult data = response.getData();
            if ("COMPLETED".equalsIgnoreCase(data.getStatus())) {

                assertFieldNotNull(data.getInvoiceId(), "invoiceId");
                assertFieldTrue(
                        data.getInvoiceId().startsWith("SAI-"),
                        "invoiceId",
                        "InvoiceId should start with SAI-");
            } else if ("PROCESSING".equalsIgnoreCase(data.getStatus())) {
                softAssert.assertNull(
                        data.getInvoiceId(), "invoiceId is null because isSync\":false");
            } else {
                // PROCESSING / PENDING → invoiceId can be null
                softAssert.assertTrue(
                        data.getInvoiceId() == null || data.getInvoiceId().isEmpty(),
                        "invoiceId should be null for non-completed orders");
            }
        }
    }

    public void assertBuyStatus(BuyStatusResponse buyStatusResponse, int expectedStatusCode) {
        int actualStatusCode = buyStatusResponse.getStatusCode();
        assertFieldsEquals(actualStatusCode, expectedStatusCode, "Status code");
        if (expectedStatusCode == 200) {

            assertFieldTrue(buyStatusResponse.isSuccess(), "success", "Expected success=true");
            // Error fields must be null
            softAssert.assertNull(
                    buyStatusResponse.getErrorCode(),
                    "errorCode should be null in success response");
            softAssert.assertNull(
                    buyStatusResponse.getErrorMessage() != null
                            ? buyStatusResponse.getErrorMessage()
                            : buyStatusResponse.getError(),
                    "error message should be null in success response");

            assertFieldNotNull(buyStatusResponse.getData(), "Data object should not be null");
            BuyStatusResponse.DataResult data = buyStatusResponse.getData();
            assertFieldNotNull(data.getOrderId(), "orderId");
            assertFieldTrue(
                    data.getOrderId().startsWith("SAO-"),
                    "orderId",
                    "orderId should start with 'SAO-'");

            assertFieldNotNull(data.getStatus(), "status should not be null");
            assertFieldNotNull(data.getInvoiceStatus(), "invoiceStatus should not be null");

            if (data.getInvoiceId() != null) {
                assertFieldTrue(data.getInvoiceId().startsWith("SAI-"), "invoiceId", "invoiceId should start with 'SAI-'");
            }
        }
    }

    public void assertSellPriceSuccessFullResponse(SellPriceResponse sellPriceResponse) {
        assertFieldTrue(sellPriceResponse.isSuccess(), "success", "Success Should be true");
        assertFieldNotNull(sellPriceResponse.getData(), "Data should not be Null");
        assertFieldNotNull(sellPriceResponse.getData().getId(), "Id Should not be null");
        assertFieldNotNull(sellPriceResponse.getData().getCode(), "Material Code should not be null");
        assertFieldNotNull(sellPriceResponse.getData().getAssetPrice(), "Asset Price must not be null on success");
    }

    public void assertSellPriceErrorResponse(ExpectedError expectedError, SellPriceResponse actualResponse) {
        String expectedErrorCode = expectedError.getErrorCode();
        String expectedErrorMessage = expectedError.getErrorMessage();
        assertFieldsEquals(actualResponse.getErrorCode(), expectedErrorCode, "Error Code");
        if (expectedErrorMessage != null) {
            assertFieldsEquals(actualResponse.getError(), expectedErrorMessage, "Error Message");
        }
    }

    public void validateSellPriceInDB(SellPriceResponse sellPriceResponse) {
        Document doc = digiGoldMongo().fetchData("digigold", "materialRate", "_id", sellPriceResponse.getData().getId(), "_id");
        String materialCode = getValueFromDocument(doc, "materialCode");
        String rateStatus = getValueFromDocument(doc, "rateStatus");
        Object finalPriceObj = doc.get("finalPrice");
        BigDecimal finalPrice;
        if (finalPriceObj instanceof Number) {
            finalPrice = BigDecimal.valueOf(((Number) finalPriceObj).longValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            softAssert.fail("Unsupported finalPrice type: " + finalPriceObj.getClass());
            return;
        }
        // Compare with request object
        assertFieldsEquals(materialCode, sellPriceResponse.getData().getCode(), "materialCode");
        assertFieldsEquals(finalPrice, sellPriceResponse.getData().getAssetPrice(), "basePrice");
        assertFieldsEquals(rateStatus, sellPriceResponse.getData().getRateStatus(), "rateStatus");
    }

    public void validateSellPrice(SellPriceResponse sellPriceResponse, int expectedStatusCode, ExpectedError expectedError) {
        int actualStatusCode = sellPriceResponse.getStatusCode();
        assertFieldsEquals(actualStatusCode, expectedStatusCode, "Status code");

        if (expectedStatusCode == 200) {
            assertSellPriceSuccessFullResponse(sellPriceResponse);
            // Validate in DB
            validateSellPriceInDB(sellPriceResponse);
        } else {
            // Validate error response body
            assertSellPriceErrorResponse(expectedError, sellPriceResponse);
        }
    }

    public void assertSellVerifySuccessFullResponse(SellVerifyResponse sellVerifyResponse) {
        assertFieldTrue(sellVerifyResponse.isSuccess(), "success", "Success Should be true");
        assertFieldNotNull(sellVerifyResponse.getData(), "Data should not be Null");
        assertFieldNotNull(sellVerifyResponse.getData().getUserId(), "userId Should not be null");
        assertFieldNotNull(sellVerifyResponse.getData().getRateId(), "rateId Should not be null");
        assertFieldNotNull(sellVerifyResponse.getData().getCode(), "Material code Should not be null");
        assertFieldNotNull(sellVerifyResponse.getData().getAmount(), "Material Code should not be null");
        assertFieldNotNull(sellVerifyResponse.getData().getOrderId(), "Order Id must not be null");
        assertFieldNotNull(sellVerifyResponse.getData().getVolume(), "Volume must not be null");
        assertFieldNotNull(sellVerifyResponse.getData().getRate(), "Rate must not be null");
    }

    public void assertSellVerifyErrorResponse(ExpectedError expectedError, SellVerifyResponse actualResponse) {
        String expectedErrorCode = expectedError.getErrorCode();
        String expectedErrorMessage = expectedError.getErrorMessage();
        assertFieldsEquals(actualResponse.getErrorCode(), expectedErrorCode, "Error Code");
        if (expectedErrorMessage != null) {
            assertFieldsEquals(actualResponse.getError(), expectedErrorMessage, "Error Message");
        }
    }

    public void validateSellVerifyInDB(SellVerifyResponse sellVerifyResponse) {
        String orderId = sellVerifyResponse.getData().getOrderId();
        BigDecimal apiAmount = sellVerifyResponse.getData().getAmount();
        String querySaleOrder = """
            select id, net_total
            from purchase_orders
            where id = ?
        """;

        try (ResultSet rs = DataBaseFactory.digiPostgres().query(querySaleOrder, orderId)) {
            Assert.assertTrue(
                    rs.next(),
                    "No purchase_orders record found for orderId: " + orderId
            );
            // Fetch DB values
            String dbOrderId = rs.getString("id");
            BigDecimal dbNetTotal = rs.getBigDecimal("net_total");

            assertFieldsEquals(dbOrderId, orderId, "OrderId mismatch");

            Assert.assertNotNull(
                    dbNetTotal,
                    "DB net_total is NULL for orderId: " + orderId
            );
            // Convert paise → rupees
            BigDecimal dbAmount = dbNetTotal.divide(
                    BigDecimal.valueOf(100),
                    2,
                    RoundingMode.HALF_UP
            );
            assertFieldsEquals(
                    dbAmount,
                    apiAmount,
                    "Amount mismatch between API and DB"
            );
            // Ensure only one row exists
            assertFieldFalse(
                    rs.next(),
                    "dbData",
                    "Multiple DB records found for orderId: " + orderId
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void validateSellVerify(SellVerifyResponse sellVerifyResponse, int expectedStatusCode, ExpectedError expectedError) {
        int actualStatusCode = sellVerifyResponse.getStatusCode();
        assertFieldsEquals(actualStatusCode, expectedStatusCode, "Status code");
        // SUCCESS CASE
        if (expectedStatusCode == 200 && sellVerifyResponse.getData() != null) {
            assertSellVerifySuccessFullResponse(sellVerifyResponse);
            validateSellVerifyInDB(sellVerifyResponse);
            return;
        }
        // FAILURE CASE
        assertSellVerifyErrorResponse(expectedError, sellVerifyResponse);
    }

    public void assertSellConfirmSuccessFullResponse(SellConfirmResponse sellConfirmResponse) {
        assertFieldTrue(sellConfirmResponse.isSuccess(), "success", "Success Should be true");
        assertFieldNotNull(sellConfirmResponse.getData(), "Data should not be Null");
        assertFieldNotNull(sellConfirmResponse.getData().getInvoiceId(), "Invoice Should not be null");
        assertFieldNotNull(sellConfirmResponse.getData().getStatus(), "Status Should not be null");
        assertFieldNotNull(sellConfirmResponse.getData().getOrderId(), "OrderId Should not be null");
    }

    public void assertSellConfirmErrorResponse(ExpectedError expectedError, SellConfirmResponse actualResponse) {
        String expectedErrorCode = expectedError.getErrorCode();
        String expectedErrorMessage = expectedError.getErrorMessage();
        assertFieldsEquals(actualResponse.getErrorCode(), expectedErrorCode, "Error Code");
        if (expectedErrorMessage != null) {
            assertFieldsEquals(actualResponse.getError(), expectedErrorMessage, "Error Message");
        }
    }

    public void validateSellConfirmInDB(SellConfirmResponse sellConfirmResponse) {
        String orderId = sellConfirmResponse.getData().getOrderId();
        String query = """
                    select status
                    from purchase_orders
                    where id = ?
                """;
        try (ResultSet rs = DataBaseFactory.digiPostgres().query(query, orderId)) {

            assertFieldTrue(
                    rs.next(),
                    "dbData",
                    "No purchase_orders record found for orderId: " + orderId
            );

            int actualStatus = rs.getInt("status");

            assertFieldsEquals(
                    actualStatus,
                    3,
                    "Purchase Order status mismatch"
            );

            // Ensure only one record exists
            assertFieldFalse(
                    rs.next(),
                    "dbData",
                    "Multiple purchase_orders records found for orderId: " + orderId
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void validateSellConfirm(SellConfirmResponse sellConfirmResponse, int expectedStatusCode, ExpectedError expectedError) {
        int actualStatusCode = sellConfirmResponse.getStatusCode();
        assertFieldsEquals(actualStatusCode, expectedStatusCode, "Status code");
        if (expectedStatusCode == 200 && sellConfirmResponse.getData() != null) {
            assertSellConfirmSuccessFullResponse(sellConfirmResponse);
            // Validate in DB
            validateSellConfirmInDB(sellConfirmResponse);
        } else {
            // Validate error response body
            assertSellConfirmErrorResponse(expectedError, sellConfirmResponse);
        }
    }

    public void assertSellStatusSuccessFullResponse(SellStatusResponse sellStatusResponse) {
        assertFieldTrue(sellStatusResponse.isSuccess(), "success", "Success Should be true");
        assertFieldNotNull(sellStatusResponse.getData(), "Data should not be Null");
        assertFieldNotNull(sellStatusResponse.getData().getInvoiceId(), "Invoice Should not be null");
        assertFieldNotNull(sellStatusResponse.getData().getStatus(), "Status Should not be null");
        assertFieldNotNull(sellStatusResponse.getData().getOrderId(), "OrderId Should not be null");
    }

    public void assertSellStatusErrorResponse(ExpectedError expectedError, SellStatusResponse actualResponse) {
        String expectedErrorCode = expectedError.getErrorCode();
        String expectedErrorMessage = expectedError.getErrorMessage();
        assertFieldsEquals(actualResponse.getErrorCode(), expectedErrorCode, "Error Code");
        if (expectedErrorMessage != null) {
            assertFieldsEquals(actualResponse.getError(), expectedErrorMessage, "Error Message");
        }
    }

    public void validateSellStatus(SellStatusResponse sellStatusResponse, int expectedStatusCode, ExpectedError expectedError) {
        int actualStatusCode = sellStatusResponse.getStatusCode();
        assertFieldsEquals(actualStatusCode, expectedStatusCode, "Status code");

        if (expectedStatusCode == 200) {
            assertSellStatusSuccessFullResponse(sellStatusResponse);
        } else {
            // Validate error response body
            assertSellStatusErrorResponse(expectedError, sellStatusResponse);
        }
    }
}
