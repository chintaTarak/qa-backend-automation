package digigold;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.jarApiAutomation.data.requestModel.digiGold.CreateUserRequest;
import org.jarApiAutomation.data.responseModel.digiGold.*;
import org.jarApiAutomation.dbConfiguration.DataBaseFactory;
import org.jarApiAutomation.utils.ApiAssertions;
import org.jarApiAutomation.utils.CommonSerializationUtil;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.asserts.SoftAssert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static digigold.DigiGoldDataProvider.ExpectedError;
import static org.jarApiAutomation.dbConfiguration.DBConstants.*;
import static org.jarApiAutomation.dbConfiguration.DBConstants.SORT_FIELD;
import static org.jarApiAutomation.dbConfiguration.DataBaseFactory.digiGoldMongo;
import static org.jarApiAutomation.dbConfiguration.DataBaseFactory.tenantMongo;
import static org.jarApiAutomation.utils.CommonUtil.getValueFromDocument;
import static testData.digiGold.DigiGoldTestData.*;
@Slf4j
public class DigiGoldValidation extends ApiAssertions
{

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
           List<UserResponse.CurrentBalance> USER_CURRENT_BALANCE = validUserResponse.getData().getCurrentBalance();

            for (UserResponse.CurrentBalance currentBalance : USER_CURRENT_BALANCE)
            {
                assertFieldsEquals(currentBalance.getMaterialCode(), MATERIAL_CODE, "materialCode");
                BigDecimal QUANTITY = currentBalance.getQuantity();
                log.info("Quantity: {}, Material code : {}", QUANTITY, MATERIAL_CODE);

            }


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
            BuyPriceResponse response,
            int expectedStatusCode,
            String expectedErrorCode,
            String expectedErrorMessage)
    {
        Document doc = null;
        if (response.getStatusCode()== HttpStatus.SC_OK && response.getData()!=null)
        {
            String rateId = response.getData().getId();
            doc =
                    digiGoldMongo()
                            .fetchData(DIGI_GOLD_DB, MATERIAL_RATE, id, rateId, SORT_FIELD);
        }

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
        /**
         * Validating  material rate from APi response and database material rate
         * **/

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

    public void assertBuyVerifyResponse(BuyVerifyResponse response, int expectedStatusCode, String expectedErrorCode, String expectedErrorMessage) throws SQLException
    {
        String apiOrderId;
        if (response != null && response.getData() != null) {
            String orderId = response.getData().getOrderId();
            Reporter.getCurrentTestResult().getTestContext().setAttribute("orderId", orderId);
            apiOrderId = response.getData().getOrderId();
            String apiUserId = response.getData().getUserId();
            BigDecimal apiAmount = response.getData().getAmount(); // 10.00
            /**
             *
             * Validating from sale orders
             **/
            String querySaleOrder = "select id, customer_id, net_total from sale_orders where id = '" + apiOrderId + "'";
            List<Map<String, Object>> dbResults = new ArrayList<>();
            ResultSet dbData = DataBaseFactory.digiPostgres().query(querySaleOrder);
            ResultSetMetaData metaData = dbData.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (dbData.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), dbData.getObject(i));
                }
                dbResults.add(row);
            }
            Assert.assertFalse(dbResults.isEmpty(), "No DB record found for orderId: " + apiOrderId);
            Map<String, Object> dbRow = dbResults.get(0);
            Assert.assertEquals(dbRow.get("id"), apiOrderId, "OrderId mismatch");
            Assert.assertEquals(dbRow.get("customer_id"), apiUserId, "CustomerId mismatch");
            BigDecimal dbNetTotal = new BigDecimal(dbRow.get("net_total").toString());
            BigDecimal dbAmount = dbNetTotal.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            Assert.assertEquals(dbAmount, apiAmount, "Amount mismatch between API and DB");
            log.info("API Amount: {}, DB Net Total (paise): {}, DB Amount (₹): {}", apiAmount, dbNetTotal, dbAmount);

            /**
             *
             * Validating from sale order items
             * Select *
             * from sale_order_items
             * where sale_order_id='orderId';
             * **/
            String querySaleOrderItems = "select * from sale_order_items where sale_order_id = '" + apiOrderId + "'";
            List<Map<String, Object>> dbResult = new ArrayList<>();
            ResultSet dbDataItem = DataBaseFactory.digiPostgres().query(querySaleOrderItems);
            ResultSetMetaData metaDataItem = dbDataItem.getMetaData();
            int columnCountItem = metaDataItem.getColumnCount();
            while (dbDataItem.next())
            {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCountItem; i++) {
                    row.put(metaDataItem.getColumnName(i), dbDataItem.getObject(i));
                }
                dbResult.add(row);
            }

            BuyVerifyResponse.DataObj api = response.getData();

            Assert.assertNotNull(api, "API response data is null");
            Assert.assertFalse(dbResult.isEmpty(), "No sale_order_items found in DB");
            Map<String, Object> item = dbResult.get(0);

            Assert.assertEquals(item.get("material_code"), api.getCode(), "Material code mismatch");
            BigDecimal dbRate = new BigDecimal(item.get("rate").toString()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            Assert.assertEquals(dbRate, api.getRate(), "Rate mismatch");
            BigDecimal dbVolume =new BigDecimal(item.get("quantity").toString()).divide(BigDecimal.valueOf(1_000_000), 6, RoundingMode.HALF_UP);
            Assert.assertEquals(dbVolume, api.getVolume(), "Volume mismatch");
            BigDecimal dbAmountItem = new BigDecimal(item.get("net_total").toString()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            Assert.assertEquals(dbAmountItem, api.getAmount(), "Amount mismatch");
            BigDecimal dbPreTax = new BigDecimal(item.get("total_amount").toString()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            Assert.assertEquals(dbPreTax, api.getPreTaxAmount(), "Pre-tax amount mismatch");
            BigDecimal dbTax = new BigDecimal(item.get("tax_added").toString()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            Assert.assertEquals(dbTax, api.getTaxAmount(), "Tax amount mismatch");
            Assert.assertEquals(item.get("status"), 1, "Sale order item status is not ACTIVE");

            log.info("Validated Sale Order Item | Rate: {}, Volume: {}, Amount: {}, PreTax: {}, Tax: {}", dbRate, dbVolume, dbAmount, dbPreTax, dbTax);



            if (expectedErrorCode != null || expectedErrorMessage != null) {
                if (!response.isSuccess()) {
                    softAssert.assertFalse(response.isSuccess(), "success should be false for error scenario");
                }
                if (expectedErrorCode != null) {
                    assertFieldsEquals(response.getErrorCode(), expectedErrorCode, "Error code mismatch");
                }
                if (expectedErrorMessage != null) {
                    String actualErrorMsg = response.getErrorMessage() != null ? response.getErrorMessage() : response.getError();
                    softAssert.assertTrue(actualErrorMsg != null && actualErrorMsg.contains(expectedErrorMessage), "Error message mismatch. Expected to contain: " + expectedErrorMessage + ", but was: " + actualErrorMsg);
                }

            }
        }
    }

    public void assertBuyConfirm(BuyConfirmResponse response, ExpectedError expectedError) throws SQLException
    {
        if (response.getStatusCode() == 200)
        {
            BuyConfirmResponse.DataResult data = response.getData();
            if ("COMPLETED".equalsIgnoreCase(data.getStatus())) {

                assertFieldNotNull(data.getInvoiceId(), "invoiceId");
                assertFieldTrue(data.getInvoiceId().startsWith("SAI-"), "invoiceId", "InvoiceId should start with SAI-");
            }
            else if ("PROCESSING".equalsIgnoreCase(data.getStatus()))
            {
                softAssert.assertNull(data.getInvoiceId(), "invoiceId is null because isSync\":false");
            }
            else
            {
                // PROCESSING / PENDING → invoiceId can be null
                softAssert.assertTrue(data.getInvoiceId() == null || data.getInvoiceId().isEmpty(), "invoiceId should be null for non-completed orders");

            }
            Assert.assertNotNull(response, "Response is null");
            Assert.assertTrue(response.isSuccess(), "Buy Confirm API failed");

            if (response != null && response.getData() != null) {

                String orderId = response.getData().getOrderId();

                // ---------- sale_orders ----------
                String query = "select status from sale_orders where id = '" + orderId + "'";
                ResultSet soRs = DataBaseFactory.digiPostgres().query(query);

                Assert.assertTrue(soRs.next(), "No sale_orders record found for orderId: " + orderId);

                int saleOrderStatus = soRs.getInt("status");
                Assert.assertEquals(saleOrderStatus, 3, "sale_orders status mismatch");

                log.info("sale_orders status validated: {}", saleOrderStatus);

                // ---------- user_ledgers ----------
                ResultSet ledgerRs = DataBaseFactory.digiPostgres().query(
                        "select quantity_after_transaction from user_ledgers " +
                                "where user_id = '" + USER_ID + "' order by created_at desc limit 1");

                Assert.assertTrue(ledgerRs.next(), "No ledger entry found for userId: " + USER_ID);
                BigDecimal dbQty = ledgerRs.getBigDecimal("quantity_after_transaction")
                        .divide(BigDecimal.valueOf(1_000_000), 6, RoundingMode.HALF_UP);
                log.info("Ledger quantity_after_transaction: {}", dbQty);
                log.info("Buy Confirm DB validation passed for orderId: {}", orderId);
            }
        }
    }

    public void assertBuyStatus(BuyStatusResponse buyStatusResponse, int expectedStatusCode)
    {
        int actualStatusCode = buyStatusResponse.getStatusCode();
        assertFieldsEquals(actualStatusCode, expectedStatusCode, "Status code");
        if (expectedStatusCode == 200)
        {
            assertFieldTrue(buyStatusResponse.isSuccess(), "success", "Expected success=true");
            // Error fields must be null
            softAssert.assertNull(buyStatusResponse.getErrorCode(), "errorCode should be null in success response");
            softAssert.assertNull(buyStatusResponse.getErrorMessage() != null ? buyStatusResponse.getErrorMessage() : buyStatusResponse.getError(),
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
                assertFieldTrue(
                        data.getInvoiceId().startsWith("SAI-"),
                        "invoiceId",
                        "invoiceId should start with 'SAI-'");
            }
        }
    }

    public void assertSellPriceSuccessFullResponse(SellPriceResponse sellPriceResponse) {
        assertFieldTrue(sellPriceResponse.isSuccess(), "success", "Success Should be true");
        assertFieldNotNull(sellPriceResponse.getData(), "Data should not be Null");
        assertFieldNotNull(sellPriceResponse.getData().getId(), "Id Should not be null");
        assertFieldNotNull(
                sellPriceResponse.getData().getCode(), "Material Code should not be null");
        assertFieldNotNull(
                sellPriceResponse.getData().getAssetPrice(),
                "Asset Price must not be null on success");
    }

    public void assertSellPriceErrorResponse(
            ExpectedError expectedError, SellPriceResponse actualResponse) {
        String expectedErrorCode = expectedError.getErrorCode();
        String expectedErrorMessage = expectedError.getErrorMessage();
        assertFieldsEquals(actualResponse.getErrorCode(), expectedErrorCode, "Error Code");
        if (expectedErrorMessage != null) {
            assertFieldsEquals(actualResponse.getError(), expectedErrorMessage, "Error Message");
        }
    }


    public void validateSellPriceInDB(SellPriceResponse sellPriceResponse) {
        Document doc =
                digiGoldMongo()
                        .fetchData(
                                "digigold",
                                "materialRate",
                                "_id",
                                sellPriceResponse.getData().getId(),
                                "_id");
        String materialCode = getValueFromDocument(doc, "materialCode");
        String rateStatus = getValueFromDocument(doc, "rateStatus");
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
        // Compare with request object
        assertFieldsEquals(materialCode, sellPriceResponse.getData().getCode(), "materialCode");
        assertFieldsEquals(finalPrice, sellPriceResponse.getData().getAssetPrice(), "basePrice");
        assertFieldsEquals(rateStatus, sellPriceResponse.getData().getRateStatus(), "rateStatus");
    }

    public void validateSellPrice(
            SellPriceResponse sellPriceResponse,
            int expectedStatusCode,
            ExpectedError expectedError) {
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
        assertFieldNotNull(
                sellVerifyResponse.getData().getCode(), "Material code Should not be null");
        assertFieldNotNull(
                sellVerifyResponse.getData().getAmount(), "Material Code should not be null");
        assertFieldNotNull(sellVerifyResponse.getData().getOrderId(), "Order Id must not be null");
        assertFieldNotNull(sellVerifyResponse.getData().getVolume(), "Volume must not be null");
        assertFieldNotNull(sellVerifyResponse.getData().getRate(), "Rate must not be null");
    }

    public void assertSellVerifyErrorResponse(
            ExpectedError expectedError, SellVerifyResponse actualResponse) {
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
        String querySaleOrder =
                """
                    select id, net_total
                    from purchase_orders
                    where id = ?
                """;

        try (ResultSet rs = DataBaseFactory.digiPostgres().query(querySaleOrder, orderId)) {
            Assert.assertTrue(rs.next(), "No purchase_orders record found for orderId: " + orderId);
            // Fetch DB values
            String dbOrderId = rs.getString("id");
            BigDecimal dbNetTotal = rs.getBigDecimal("net_total");

            assertFieldsEquals(dbOrderId, orderId, "OrderId mismatch");

            Assert.assertNotNull(dbNetTotal, "DB net_total is NULL for orderId: " + orderId);
            // Convert paise → rupees
            BigDecimal dbAmount =
                    dbNetTotal.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            assertFieldsEquals(dbAmount, apiAmount, "Amount mismatch between API and DB");
            // Ensure only one row exists
            assertFieldFalse(
                    rs.next(), "dbData", "Multiple DB records found for orderId: " + orderId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void validateSellVerify(
            SellVerifyResponse sellVerifyResponse,
            int expectedStatusCode,
            ExpectedError expectedError) {
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
        assertFieldNotNull(
                sellConfirmResponse.getData().getInvoiceId(), "Invoice Should not be null");
        assertFieldNotNull(sellConfirmResponse.getData().getStatus(), "Status Should not be null");
        assertFieldNotNull(
                sellConfirmResponse.getData().getOrderId(), "OrderId Should not be null");
    }

    public void assertSellConfirmErrorResponse(
            ExpectedError expectedError, SellConfirmResponse actualResponse) {
        String expectedErrorCode = expectedError.getErrorCode();
        String expectedErrorMessage = expectedError.getErrorMessage();
        assertFieldsEquals(actualResponse.getErrorCode(), expectedErrorCode, "Error Code");
        if (expectedErrorMessage != null) {
            assertFieldsEquals(actualResponse.getError(), expectedErrorMessage, "Error Message");
        }
    }

    public void validateSellConfirmInDB(SellConfirmResponse sellConfirmResponse) {
        String orderId = sellConfirmResponse.getData().getOrderId();
        String query =
                """
                    select status
                    from purchase_orders
                    where id = ?
                """;
        try (ResultSet rs = DataBaseFactory.digiPostgres().query(query, orderId)) {

            assertFieldTrue(
                    rs.next(), "dbData", "No purchase_orders record found for orderId: " + orderId);

            int actualStatus = rs.getInt("status");

            assertFieldsEquals(actualStatus, 3, "Purchase Order status mismatch");

            // Ensure only one record exists
            assertFieldFalse(
                    rs.next(),
                    "dbData",
                    "Multiple purchase_orders records found for orderId: " + orderId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void validateSellConfirm(
            SellConfirmResponse sellConfirmResponse,
            int expectedStatusCode,
            ExpectedError expectedError) {
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
        assertFieldNotNull(
                sellStatusResponse.getData().getInvoiceId(), "Invoice Should not be null");
        assertFieldNotNull(sellStatusResponse.getData().getStatus(), "Status Should not be null");
        assertFieldNotNull(sellStatusResponse.getData().getOrderId(), "OrderId Should not be null");
    }

    public void assertSellStatusErrorResponse(
            ExpectedError expectedError, SellStatusResponse actualResponse) {
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
        } else
        {
            // Validate error response body
            assertSellStatusErrorResponse(expectedError, sellStatusResponse);
        }
    }

    public void validateProductWithDB(
            String skuId,
            String name,
            String materialType,
            BigDecimal weight,
            String weightUOM,
            String hsnCode,
            BigDecimal height,
            BigDecimal length,
            BigDecimal width,
            String unit) {

        Document doc = digiGoldMongo().fetchData("digigold", "material", "code", skuId, "code");
        if (doc == null) {
            throw new AssertionError("No DB record found for skuId: " + skuId);
        }
        boolean isDeliveryAllowed = doc.getBoolean("isDeliveryAllowed");
        if (isDeliveryAllowed) {
            String dbSkuId = doc.getString("code");
            String dbName = doc.getString("name");
            String dbMaterialType = doc.getString("materialType");
            BigDecimal dbWeight = null;
            Object weightObj = doc.get("weight");
            if (weightObj != null) {
                dbWeight = new BigDecimal(weightObj.toString()).stripTrailingZeros();
                ;
            }
            String dbWeightUOM = doc.getString("displayUnit");
            String dbHsnCode = null;
            Object hsnObj = doc.get("hsnCode");
            if (hsnObj != null) {
                dbHsnCode = hsnObj.toString();
            }
            // --- dimensions ---
            Document dimensions = (Document) doc.get("productDimensions");
            BigDecimal dbHeight = null;
            BigDecimal dbLength = null;
            BigDecimal dbWidth = null;
            String dbUnit = null;
            if (dimensions != null) {
                Object heightObj = dimensions.get("height");
                dbHeight = new BigDecimal(heightObj.toString());
                Object widthObj = dimensions.get("width");
                dbWidth = new BigDecimal(widthObj.toString());
                Object lengthObj = dimensions.get("length");
                dbLength = new BigDecimal(lengthObj.toString());
                dbUnit = dimensions.getString("unit");
            }
            // --- Assertions ---
            assertFieldsEquals(dbSkuId, skuId, "SKU mismatch");
            assertFieldsEquals(dbName, name, "Name mismatch");
            assertFieldsEquals(dbMaterialType, materialType, "Material type mismatch");
            assertFieldsEquals(dbWeight, weight, "Weight mismatch");
            assertFieldsEquals(dbWeightUOM, weightUOM, "Weight UOM mismatch");
            assertFieldsEquals(dbHsnCode, hsnCode, "HSN mismatch");
            assertFieldsEquals(dbHeight, height, "Height mismatch");
            assertFieldsEquals(dbLength, length, "Length mismatch");
            assertFieldsEquals(dbWidth, width, "Width mismatch");
            assertFieldsEquals(dbUnit, unit, "Dimension unit mismatch");
        }
    }

    public void validateAllProductErrorResponse(JsonNode response, ExpectedError expectedError) {
        boolean success = response.path("success").asBoolean();
        assertFieldsEquals(success, false, "Success flag");
        // Validate error code
        String actualErrorCode = response.path("errorCode").asText();
        String expectedErrorCode = expectedError.getErrorCode();
        assertFieldsEquals(actualErrorCode, expectedErrorCode, "Error Code");
        // Validate error message
        String expectedErrorMessage = expectedError.getErrorMessage();
        if (expectedErrorMessage != null) {
            String actualErrorMessage = response.path("error").asText();
            assertFieldsEquals(actualErrorMessage, expectedErrorMessage, "Error Message");
        }
    }

    public void validateProducts(
            JsonNode response,
            int statusCode,
            int expectedStatusCode,
            ExpectedError expectedError) {
        if (expectedStatusCode == statusCode) {
            JsonNode items = response.path("data").path("items");
            for (JsonNode item : items) {
                String skuId = item.path("skuId").asText();
                String name = item.path("name").asText();
                String materialType = item.path("materialType").asText();
                BigDecimal weight =
                        item.path("weight")
                                .decimalValue()
                                .multiply(BigDecimal.valueOf(1_000_000))
                                .stripTrailingZeros();
                String weightUOM = item.path("weightUOM").asText();
                String hsnCode = item.path("hsnCode").asText();
                JsonNode dimensions = item.path("productDimensions");
                BigDecimal height = dimensions.path("height").decimalValue();
                BigDecimal length = dimensions.path("length").decimalValue();
                BigDecimal width = dimensions.path("width").decimalValue();
                String unit = dimensions.path("unit").asText();
                // validate in DB
                validateProductWithDB(
                        skuId,
                        name,
                        materialType,
                        weight,
                        weightUOM,
                        hsnCode,
                        height,
                        length,
                        width,
                        unit);
            }
        } else {
            validateAllProductErrorResponse(response, expectedError);
        }
    }
    public void assertInvoiceDetails(InvoiceResponse  invoiceResponse, String expectedErrorCode)
    {
        if (invoiceResponse.isSuccess()&& invoiceResponse.getData()!=null)
        {
            assertFieldTrue(true, "success", "Expected success=true");
            softAssert.assertNull(invoiceResponse.getErrorCode(), "errorCode should be null in success response");
            softAssert.assertNull(invoiceResponse.getErrorMessage() != null ? invoiceResponse.getErrorMessage() : invoiceResponse.getError(),
                    "error message should be null in success response");
            assertFieldNotNull(invoiceResponse.getData(), "Data object should not be null");
        }
    }

    public void validateDeliveryOrderInDB(DeliveryOrderResponse deliveryOrderResponse)
    {
        try {
            String orderId = deliveryOrderResponse.getData().getOrderId();
            String userId = deliveryOrderResponse.getData().getUserId();

            BigDecimal apiPrice = deliveryOrderResponse.getData().getProducts().getPriceDetails().getPrice();
            BigDecimal apiMakingCharges = deliveryOrderResponse.getData().getProducts().getPriceDetails().getMakingCharges();
            BigDecimal apitaxAdded = deliveryOrderResponse.getData().getProducts().getPriceDetails().getTaxAdded();
            BigDecimal apiNetTotalRupees = apiPrice.add(apiMakingCharges).add(apitaxAdded);
            log.info("API net total in Rupees | orderId={}, netTotal={}", orderId, apiNetTotalRupees);
            String queryDeliveryOrder = """
                        select id, customer_id,net_total
                        from delivery_orders
                        where customer_id = ?
                        order by created_at desc
                    """;
            log.debug("Executing DB query for delivery_orders with customer_id={}", userId);

            ResultSet rs = DataBaseFactory.digiPostgres().query(queryDeliveryOrder, userId);
            Assert.assertTrue(rs.next(), "No delivery_orders record found for orderId: " + orderId);
            String dbOrderId = rs.getString("id");
            String dbCustomerId = rs.getString("customer_id");
            BigDecimal dbNetTotalAmountPaise = rs.getBigDecimal("net_total");
            BigDecimal dbNetTotalAmountRupees = dbNetTotalAmountPaise.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            log.info("DB values | dbOrderId={}, dbCustomerId={}, dbTotalAmountPaise={}", dbOrderId, dbCustomerId,dbNetTotalAmountRupees);
            assertFieldsEquals(dbOrderId, orderId, "OrderId mismatch");
            assertFieldsEquals(apiNetTotalRupees,dbNetTotalAmountRupees,"Amount mismatch");
            assertFieldsEquals(dbCustomerId, userId, "CustomerId mismatch");

        }
        catch (SQLException e)
        {
            log.error("SQL Exception while validating delivery order in DB | orderId={}", deliveryOrderResponse.getData().getOrderId(), e);
            throw new RuntimeException(e);
        }

    }


    public void assertDeliveryOrder(DeliveryOrderResponse  deliveryOrderResponse, ExpectedError expectedErrorCode)
    {
        if (deliveryOrderResponse.isSuccess()&& deliveryOrderResponse.getData()!=null)
        {
            assertFieldTrue(true, "success", "Expected success=true");
            softAssert.assertNull(deliveryOrderResponse.getErrorCode(), "errorCode should be null in success response");
            softAssert.assertNull(deliveryOrderResponse.getErrorMessage() != null ? deliveryOrderResponse.getErrorMessage() : deliveryOrderResponse.getError(),
                    "error message should be null in success response");
            assertFieldNotNull(deliveryOrderResponse.getData(), "Data object should not be null");
            validateDeliveryOrderInDB(deliveryOrderResponse);

        }
    }
    public void assertDeliveryOrderConfirmation(DeliveryOrderConfirmResponse deliveryOrderConfirmResponse, ExpectedError expectedErrorCode)
    {
        if (deliveryOrderConfirmResponse.isSuccess() && deliveryOrderConfirmResponse.getData() != null)
        {
            assertFieldTrue(true, "success", "Expected success=true");
            assertFieldNull(deliveryOrderConfirmResponse.getErrorCode(), "errorCode should be null in success response");
            assertFieldNull(deliveryOrderConfirmResponse.getErrorMessage() != null ? deliveryOrderConfirmResponse.getErrorMessage() : deliveryOrderConfirmResponse.getError(), "error message should be null in success response");
            assertFieldNotNull(deliveryOrderConfirmResponse.getData(), "Data object should not be null");
            String orderId = deliveryOrderConfirmResponse.getData().getOrderId();
            String invoiceId = deliveryOrderConfirmResponse.getData().getInvoiceId();
            try {
                log.info("====== DB VALIDATION START | orderId={} | invoiceId={} ======",
                        orderId, invoiceId);

                validateDeliveryOrderItems(orderId);
                validateInvoice(invoiceId, orderId);
                validateInvoiceItems(invoiceId);
                validateDeliveryNoteAndItems(orderId);
                log.info("====== DB VALIDATION SUCCESS | orderId={} ======", orderId);

            }
            catch (Exception e)
            {
                log.error("DB validation failed | orderId={}", orderId, e);
                throw new RuntimeException(e);
            }
        }

    }
    private void validateDeliveryOrderItems(String orderId) throws SQLException
    {
        String sql = """
        SELECT net_total
        FROM delivery_order_items
        WHERE delivery_order_id = ?""";

        ResultSet rs = DataBaseFactory.digiPostgres().query(sql, orderId);
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        while (rs.next())
        {
            BigDecimal val = new BigDecimal(rs.getObject(1).toString());
            sum = sum.add(val);
            count++;
            log.info("Order Item | orderId={} | net_total={}", orderId, val);
        }
        assertFieldTrue(count >= 1, "delivery_order_items count", "No delivery_order_items found for orderId=" + orderId);
        log.info("Order Items SUM | orderId={} | total={}", orderId, sum);
    }
    private void validateInvoice(String invoiceId, String orderId) throws SQLException {
        String sql = """
                SELECT delivery_order_id, net_total
                FROM delivery_invoices
                WHERE id = ? """;

        ResultSet rs = DataBaseFactory.digiPostgres().query(sql, invoiceId);
        assertFieldTrue(rs.next(), "delivery_invoice existence", "Invoice not found for invoiceId=" + invoiceId);
        if (rs.next())
        {
            assertFieldsEquals(rs.getString("delivery_order_id"), orderId, "Invoice mapped delivery_order_id");
            assertFieldNotNull(rs.getBigDecimal("net_total"), "invoice net_total");
            log.info("Invoice OK | invoiceId={} | net_total={}", invoiceId, rs.getBigDecimal("net_total"));
        }
    }
    private void validateInvoiceItems(String invoiceId) throws SQLException
    {
        String sql = """
        SELECT net_total
        FROM delivery_invoice_items
        WHERE delivery_invoice_id = ?""";

        ResultSet rs = DataBaseFactory.digiPostgres().query(sql, invoiceId);
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        while (rs.next())
        {
            BigDecimal val = new BigDecimal(rs.getObject(1).toString());
            sum = sum.add(val);
            count++;
            log.info("Invoice Item | invoiceId={} | net_total={}", invoiceId, val);
        }
        assertFieldTrue(count >= 1, "delivery_invoice_items count", "No invoice items found for invoiceId=" + invoiceId   );
        log.info("Invoice Items SUM | invoiceId={} | total={}", invoiceId, sum);
    }
    private void validateDeliveryNoteAndItems(String orderId) throws SQLException
    {
        String noteSql = """
        SELECT id
        FROM delivery_notes
        WHERE delivery_order_id = ?
        ORDER BY created_at DESC """;

        ResultSet noteRs = DataBaseFactory.digiPostgres().query(noteSql, orderId);
        assertFieldTrue(noteRs.next(), "delivery_note existence", "Delivery note not found for orderId=" + orderId );
        String noteId = noteRs.getString("id");
        log.info("Delivery Note | noteId={}", noteId);

        String itemSql = """
        SELECT material_code, quantity
        FROM delivery_note_items
        WHERE delivery_note_id = ? """;

        ResultSet itemRs = DataBaseFactory.digiPostgres().query(itemSql, noteId);
        int count = 0;
        while (itemRs.next()) {
            log.info("Note Item | noteId={} | material={} | qty={}", noteId,
                    itemRs.getString("material_code"),
                    itemRs.getBigDecimal("quantity")); count++;
        }

        assertFieldTrue(count >= 1, "delivery_note_items count", "No delivery_note_items found for noteId=" + noteId );
    }

    public void assertDeliveryOrderDetails(DeliveryOrderResponse  deliveryOrderResponse, ExpectedError expectedErrorCode)
    {
        if (deliveryOrderResponse.isSuccess()&& deliveryOrderResponse.getData()!=null)
        {
            assertFieldTrue(true, "success", "Expected success=true");
            assertFieldNull(deliveryOrderResponse.getErrorCode(), "errorCode should be null in success response");
            assertFieldNull(deliveryOrderResponse.getErrorMessage() != null ? deliveryOrderResponse.getErrorMessage() : deliveryOrderResponse.getError(),
                    "error message should be null in success response");
            assertFieldNotNull(deliveryOrderResponse.getData(), "Data object should not be null");

        }

    }

}





