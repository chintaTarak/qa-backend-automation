package digigold;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.jarApiAutomation.data.requestModel.digiGold.BuyConfirmRequest;
import org.jarApiAutomation.data.requestModel.digiGold.BuyVerifyRequest;
import org.jarApiAutomation.utils.CommonUtil;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import java.math.BigDecimal;
import java.util.Map;

import static digigold.DigiGoldDataProvider.ExpectedError.*;
import static org.jarApiAutomation.data.requestModel.digiGold.CreateUserRequest.createUser;
import static testData.digiGold.DigiGoldTestData.*;


@Slf4j
public class DigiGoldDataProvider {


    @AllArgsConstructor
    @Getter
    enum ExpectedError {

        ACCESS_DENIED("403", "Access Denied", 400),
        FORBIDDEN(null, null, 403),
        INVALID_USER_REF("20023", "User ref id or phone number is required", 400),
        USER_NOT_EXIST("20014", "User does not exist", 400),
        QUERY_PARAM_MISSING("10001", "Required query parameter userId is not present", 400),
        INVALID_MATERIAL_CODE("30010", "Invalid material code", 400),
        MATERIAL_CODE_REQUIRED("30011", "Material code is required", 400),
        INVALID_RATE_ID("30010", "Invalid rate id", 400),
        INVALID_AMOUNT("30001", "Invalid amount", 400),
        INVALID_QUANTITY("30002", "Invalid quantity", 400),
        MATERIAL_NOT_COMMISSIONED("10069", "Material is not commissioned",400 ),
        USERID_REQUIRED("10002", "Field userId is required", 400),
        AMOUNT_REQUIRED("10002", "Field amount is required", 400),
        AMOUNT_ZER0("10136","Amount can not be zero",400),
        INVALID_VOLUME("10110","Invalid volume",400),
        MERCHANT_ORDER_ALREADY_EXIST("10108", "already exist", 400),
        ORDER_NOT_FOUND("10002", "Field orderId is required", 400),
        INVALID_RATE("10105","Invalid rate id INVALID_RATE",400),
        SALE_ORDER_DRAFT("10052","Sale order is not in DRAFT status",400),
        SALE_ORDER_NOT_FOUND("10051","Sale order  not found",400),
        ID_PHONE_REQUIRED("20013","User id or phone number is required",400),
        ORDER_ALREADY_CONFIRMED("10109", "Order already confirmed", 400);

        private final String errorCode;
        private final String errorMessage;
        private final int expectedStatusCode;

    }


    @DataProvider(name = "userCreationScenarios")
    public Object[][] userDetails()
    {
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

    @DataProvider(name = "buyPriceDataScenarios")
    public Object[][] buyPriceData() {

        return new Object[][]{

                // Invalid material code → Validation failure
                {"INVALID_CODE", X_TENANT_INFO, INVALID_MATERIAL_CODE.getExpectedStatusCode(), MATERIAL_NOT_COMMISSIONED.getErrorCode(), MATERIAL_NOT_COMMISSIONED.getErrorMessage(), false},

                // Empty material code → Validation failure
                {"", X_TENANT_INFO, MATERIAL_NOT_COMMISSIONED.getExpectedStatusCode(), MATERIAL_NOT_COMMISSIONED.getErrorCode(), MATERIAL_NOT_COMMISSIONED.getErrorMessage(), false},

                // Missing X-Tenant-Info → Access denied
                {MATERIAL_CODE, "", ACCESS_DENIED.getExpectedStatusCode(), ACCESS_DENIED.getErrorCode(), ACCESS_DENIED.getErrorMessage(), false},

                // Missing material code & tenant info → Validation failure
                {"", "", ACCESS_DENIED.getExpectedStatusCode(), ACCESS_DENIED.getErrorCode(), ACCESS_DENIED.getErrorMessage(), false},

                // Valid request → Success + DB validation
                {MATERIAL_CODE, X_TENANT_INFO, 200, null, null, true},
        };
    }
    @DataProvider(name = "buyVerifyScenarios")
    public Object[][] buyVerifyScenarios(ITestContext context) {
        String rateId = (String) context.getAttribute("rateId");

        return new Object[][] {

                // ================= INVALID RATE ID =================
                {BuyVerifyRequest.buyVerifyRequestPayload("INVALID_RATE", USER_ID, new BigDecimal("0.9"), new BigDecimal("10"), MATERIAL_CODE, "GoldBuy-" + System.nanoTime(), CALCULATION_TYPE_AMOUNT),X_TENANT_INFO,
                        INVALID_RATE.getExpectedStatusCode(), INVALID_RATE.getErrorCode(), INVALID_RATE.getErrorMessage()},

                // ================= INVALID USER ID =================
              {BuyVerifyRequest.buyVerifyRequestPayload(rateId, "INVALID_USER",new BigDecimal("0.3") ,new BigDecimal("10"),MATERIAL_CODE, "GoldBuy-" + System.nanoTime(), CALCULATION_TYPE_AMOUNT),X_TENANT_INFO,
                USER_NOT_EXIST.getExpectedStatusCode(), USER_NOT_EXIST.getErrorCode(), USER_NOT_EXIST.getErrorMessage()},

                // ================= NEGATIVE AMOUNT =================
                {BuyVerifyRequest.buyVerifyRequestPayload(rateId, USER_ID, new BigDecimal("0.4"), new BigDecimal("-10"), MATERIAL_CODE, "GoldBuy-" + System.nanoTime(), CALCULATION_TYPE_AMOUNT),X_TENANT_INFO,
                AMOUNT_ZER0.getExpectedStatusCode(), AMOUNT_ZER0.getErrorCode(), AMOUNT_ZER0.getErrorMessage()},

                // ================= MISSING VOLUME (BY_QUANTITY) =================
                {BuyVerifyRequest.buyVerifyRequestPayload(rateId, USER_ID, BigDecimal.ZERO, null, MATERIAL_CODE, "GoldBuy-" + System.nanoTime(), CALCULATION_TYPE_QUANTITY),X_TENANT_INFO,
                        AMOUNT_REQUIRED.getExpectedStatusCode(), AMOUNT_REQUIRED.getErrorCode(), AMOUNT_REQUIRED.getErrorMessage()},

                // ================= MATERIAL NOT COMMISSIONED =================
               {BuyVerifyRequest.buyVerifyRequestPayload(rateId, USER_ID, new BigDecimal("0.44"), new BigDecimal("10"), "INVALID_CODE", "GoldBuy-" + System.nanoTime(), CALCULATION_TYPE_AMOUNT),X_TENANT_INFO,
                        MATERIAL_NOT_COMMISSIONED.getExpectedStatusCode(), MATERIAL_NOT_COMMISSIONED.getErrorCode(), MATERIAL_NOT_COMMISSIONED.getErrorMessage()},
                // ================= QUERY PARAMS MISSING =================
                {BuyVerifyRequest.buyVerifyRequestPayload(rateId, USER_ID, new BigDecimal("0.44"), new BigDecimal("10"), MATERIAL_CODE, "GoldBuy-" + System.nanoTime(), CALCULATION_TYPE_AMOUNT),"",
                        ACCESS_DENIED.getExpectedStatusCode(), ACCESS_DENIED.getErrorCode(), ACCESS_DENIED.getErrorMessage()},
                // ================= HAPPY CASE =================
                {BuyVerifyRequest.buyVerifyRequestPayload(rateId, USER_ID, BigDecimal.ZERO, new BigDecimal("10"), MATERIAL_CODE, "GoldBuyVerify-" + System.nanoTime(), CALCULATION_TYPE_AMOUNT),X_TENANT_INFO,
                        200, null, null}
        };
    }



    @DataProvider(name = "buyConfirmData")
    public Object[][] buyConfirmData(ITestContext context) {

        String validOrderId = (String) context.getAttribute("orderId");

        return new Object[][]{

                /* ================= MISSING USER ID ================= */
                {BuyConfirmRequest.buyConfirmPayload("", validOrderId, MATERIAL_CODE, false), X_TENANT_INFO,USERID_REQUIRED},

                /* ================= INVALID USER ID ================= */
                {BuyConfirmRequest.buyConfirmPayload("76567565fgc", validOrderId, MATERIAL_CODE, false),X_TENANT_INFO,USER_NOT_EXIST},

                /* ================= MISSING ORDER ID ================= */
                {BuyConfirmRequest.buyConfirmPayload(USER_ID, "", MATERIAL_CODE, false), X_TENANT_INFO,ORDER_NOT_FOUND},

                /* ================= MISSING MATERIAL CODE ================= */
                {BuyConfirmRequest.buyConfirmPayload(USER_ID, "2387648273", MATERIAL_CODE, false), X_TENANT_INFO,SALE_ORDER_NOT_FOUND},
                /* ================= QUERY PARAMS MISSING ================= */
                {BuyConfirmRequest.buyConfirmPayload(USER_ID, validOrderId, MATERIAL_CODE, false), "",ACCESS_DENIED},

                /* ================= VALID SYNC CONFIRM ================= */
                {BuyConfirmRequest.buyConfirmPayload(USER_ID, validOrderId, MATERIAL_CODE, false), X_TENANT_INFO,null},

        };
    }
    @DataProvider(name = "buyStatusData")
    public Object[][] buyStatusData(ITestContext context) {

        String validOrderId = (String) context.getAttribute("orderId");

        return new Object[][]{

                /* ================= MISSING ORDER ID ================= */
                {"", USER_ID,X_TENANT_INFO ,SALE_ORDER_NOT_FOUND},

                /* ================= MISSING USER ID ================= */
                {validOrderId, "", X_TENANT_INFO,ID_PHONE_REQUIRED},

                /* ================= INVALID ORDER ID ================= */
                {"INVALID_ORDER_ID", USER_ID, X_TENANT_INFO,SALE_ORDER_NOT_FOUND},

                /* ================= INVALID USER ID ================= */
                {validOrderId, "INVALID_USER",X_TENANT_INFO,USER_NOT_EXIST},
                /* ================= MISSING QUERY MISSING  ================= */
                {validOrderId, "INVALID_USER","",ACCESS_DENIED},

                {null, null,X_TENANT_INFO,ACCESS_DENIED},
                /* ================= VALID BUY STATUS ================= */
                {validOrderId, USER_ID, X_TENANT_INFO,null}
        };
    }









}
