package changejar.autopayService;

import static org.jarApiAutomation.dbConfiguration.DBConstants.*;
import static testData.Autopay.TestDataAutopay.*;

import base.BaseTest;
import changejar.authService.AuthServiceHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.jarApiAutomation.data.requestModel.autopay.InitiateAutopayRequest;
import org.jarApiAutomation.data.requestModel.autopay.StatusAutopayRequest;
import org.jarApiAutomation.data.responseModel.autopay.InitiateAutopayResponse;
import org.jarApiAutomation.data.responseModel.autopay.StatusAutopayResponse;
import org.jarApiAutomation.dbConfiguration.DBConstants;
import org.jarApiAutomation.dbConfiguration.DataBaseFactory;
import org.jarApiAutomation.dbConfiguration.MongoDBUtils;
import org.jarApiAutomation.utils.ApiAssertions;
import org.jarApiAutomation.utils.JwtUtils;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

@Slf4j
public class InitiateAutopayTest extends BaseTest {

    public static class AutopayFlowContext {
        public static String subscriptionId;
        public static String authReqId;
    }

    private final AutopayMethods autopayMethods = new AutopayMethods();

    protected SoftAssert softAssert;
    protected ApiAssertions apiAssertions;

    @BeforeMethod
    public void setup() {
        softAssert = new SoftAssert();
        apiAssertions = new ApiAssertions(softAssert);
    }

    private String token;
    @BeforeMethod
    public void setToken(ITestContext context) {
        AuthServiceHelper authHelper = new AuthServiceHelper();
        token = authHelper.generateToken(context);
    }

    @Test(
            priority = 1,
            groups = {"initiate-autopay"},
            description = "Test Autopay Initiate for SUCCESS, PENDING, FAILED states",
            dataProvider = "autopayStates",
            dataProviderClass = AutopayDataProvider.class)
    public void testInitiateAutopay(
            double mandateAmount,
            String mockServerStatus,
            String expectedState,
            ITestContext context) {

        // Fetch token from suite
        AutopayValidation autopayValidation = new AutopayValidation(softAssert);
        apiAssertions.assertFieldNotNull(token, "AUTH_TOKEN");
        String expectedUserId = JwtUtils.extractUserId(token);

        try {
            // Build request
            InitiateAutopayRequest request =
                    InitiateAutopayRequest.buildRequest(
                            PROVIDER,
                            mandateAmount,
                            AUTH_WORKFLOW_TYPE,
                            PACKAGE_NAME,
                            PHONEPE_VERSION_CODE,
                            SUBSCRIPTION_TYPE,
                            SUBS_SETUP_TYPE,
                            MANDATE_SETUP_FROM,
                            mockServerStatus);

            // Call API
            InitiateAutopayResponse response =
                    autopayMethods.initiateAutopay(
                            request, Map.of("Authorization", "Bearer " + token));
            log.info("API Response for {}: {}", mockServerStatus, response);
            // Basic validations

            autopayValidation.assertInitiateAutopayResponse(response, expectedState);

            // ********************************************************************
            // New DB Validation using API response subscriptionId
            // ********************************************************************
            AutopayFlowContext.subscriptionId = response.getData().getId();

            Document dbRecord =
                    DataBaseFactory.changeJarMongo()
                            .fetchData(
                                    DB_CHANGEJAR,
                                    COLLECTION_SUBSCRIPTIONS,
                                    "_id",
                                    AutopayFlowContext.subscriptionId,
                                    "createdAt");

            // Validate all fields
            autopayValidation.validateSubscriptionDbBasicFields(
                    dbRecord, expectedUserId, mandateAmount, expectedState);

            // Handle authWorkflowType for PENNY_DROP (1st 1 hour case)
            String authWorkflowType = dbRecord.getString("authWorkflowType");
            Date updatedAt = dbRecord.getDate("updatedAt"); // IMPORTANT: Date, not Long

            long now = System.currentTimeMillis();

            apiAssertions.assertFieldsEquals(authWorkflowType, "TRANSACTION", "authWorkflowType");

            //  Capture subscriptionId and authReqId only if CREATED
            if ("CREATED".equalsIgnoreCase(dbRecord.getString("state"))) {
                AutopayFlowContext.subscriptionId = dbRecord.getObjectId("_id").toString();
                AutopayFlowContext.authReqId = dbRecord.getString("authRequestId");
                apiAssertions.assertFieldNotNull(AutopayFlowContext.authReqId, "authReqId");
                log.info(
                        "Captured SUCCESS subscriptionId: {}, authReqId: {}",
                        AutopayFlowContext.subscriptionId,
                        AutopayFlowContext.authReqId);
            } else {
                log.info("Skipping capture for non-CREATED state: {}", dbRecord.getString("state"));
            }

        } catch (Exception e) {
            log.error(
                    "Autopay Initiate Test Failed for {} state: {}",
                    mockServerStatus,
                    e.getMessage());
            softAssert.fail("Test Exception: " + e.getMessage());
        } finally {
            apiAssertions.assertAll();
        }
    }

    @Test(priority = 2, description = "Test Autopay Initiate Negative Cases")
    public void testInitiateAutopayNegativeCases(ITestContext context) {

        apiAssertions.assertFieldNotNull(token, "AUTH_TOKEN");

        try {
            ObjectMapper mapper = new ObjectMapper();

            // ----- Negative Case 1: mandateAmount less than minimum (e.g., 5) -----
            InitiateAutopayRequest mandateAmountReq =
                    InitiateAutopayRequest.buildRequest(
                            PROVIDER,
                            INVALID_AMOUNT,
                            AUTH_WORKFLOW_TYPE,
                            PACKAGE_NAME,
                            PHONEPE_VERSION_CODE,
                            SUBSCRIPTION_TYPE,
                            SUBS_SETUP_TYPE,
                            MANDATE_SETUP_FROM,
                            "SUCCESS");
            JsonNode mandateAmountRes = autopayMethods.initiateAutopayRaw(mandateAmountReq, token);

            if (mandateAmountRes.has("success") && !mandateAmountRes.get("success").isNull()) {
                apiAssertions.assertFieldTrue(
                        !mandateAmountRes.get("success").asBoolean(),
                        "success",
                        "API should fail for mandateAmount < 10");
            }
            if (mandateAmountRes.has("status") && !mandateAmountRes.get("status").isNull()) {
                apiAssertions.assertFieldsEquals(
                        mandateAmountRes.get("status").asText(), "error", "status");
            }
            if (mandateAmountRes.has("errorMessage")
                    && !mandateAmountRes.get("errorMessage").isNull()) {
                apiAssertions.assertFieldsEquals(
                        mandateAmountRes.get("errorMessage").asText(),
                        "Please enter amount above ₹10",
                        "errorMessage");
            }

            // ----- Negative Case 2: Invalid provider -----
            InitiateAutopayRequest invalidProviderReq =
                    InitiateAutopayRequest.buildRequest(
                            INVALID_PROVIDER,
                            MANDATE_AMOUNT,
                            AUTH_WORKFLOW_TYPE,
                            PACKAGE_NAME,
                            PHONEPE_VERSION_CODE,
                            SUBSCRIPTION_TYPE,
                            SUBS_SETUP_TYPE,
                            MANDATE_SETUP_FROM,
                            "SUCCESS");
            JsonNode invalidProviderRes =
                    autopayMethods.initiateAutopayRaw(invalidProviderReq, token);

            if (invalidProviderRes.has("success") && !invalidProviderRes.get("success").isNull()) {
                apiAssertions.assertFieldTrue(
                        !invalidProviderRes.get("success").asBoolean(),
                        "success",
                        "API should fail for invalid provider");
            }
            if (invalidProviderRes.has("status") && !invalidProviderRes.get("status").isNull()) {
                apiAssertions.assertFieldsEquals(
                        invalidProviderRes.get("status").asText(), "error", "status");
            }
            if (invalidProviderRes.has("errorMessage")
                    && !invalidProviderRes.get("errorMessage").isNull()) {
                apiAssertions.assertFieldsEquals(
                        invalidProviderRes.get("errorMessage").asText(),
                        "Bad Request",
                        "errorMessage");
            }

            // ----- Negative Case 3: Missing authWorkflowType -----
            InitiateAutopayRequest missingAuthWorkflowReq =
                    InitiateAutopayRequest.buildRequest(
                            PROVIDER,
                            MANDATE_AMOUNT,
                            "", // missing
                            PACKAGE_NAME,
                            PHONEPE_VERSION_CODE,
                            SUBSCRIPTION_TYPE,
                            SUBS_SETUP_TYPE,
                            MANDATE_SETUP_FROM,
                            "SUCCESS");
            JsonNode missingAuthWorkflowRes =
                    autopayMethods.initiateAutopayRaw(missingAuthWorkflowReq, token);

            if (missingAuthWorkflowRes.has("success")
                    && !missingAuthWorkflowRes.get("success").isNull()) {
                apiAssertions.assertFieldTrue(
                        !missingAuthWorkflowRes.get("success").asBoolean(),
                        "success",
                        "API should fail when authWorkflowType is missing");
            }
            if (missingAuthWorkflowRes.has("status")
                    && !missingAuthWorkflowRes.get("status").isNull()) {
                apiAssertions.assertFieldsEquals(
                        missingAuthWorkflowRes.get("status").asText(), "error", "status");
            }
            if (missingAuthWorkflowRes.has("errorMessage")
                    && !missingAuthWorkflowRes.get("errorMessage").isNull()) {
                apiAssertions.assertFieldsEquals(
                        missingAuthWorkflowRes.get("errorMessage").asText(),
                        "Bad Request",
                        "errorMessage");
            }

            // ----- Negative Case 4: Missing subscriptionType -----
            InitiateAutopayRequest missingSubscriptionReq =
                    InitiateAutopayRequest.buildRequest(
                            PROVIDER,
                            MANDATE_AMOUNT,
                            AUTH_WORKFLOW_TYPE,
                            PACKAGE_NAME,
                            PHONEPE_VERSION_CODE,
                            "", // missing subscriptionType
                            SUBS_SETUP_TYPE,
                            MANDATE_SETUP_FROM,
                            "SUCCESS");
            JsonNode missingSubscriptionRes =
                    autopayMethods.initiateAutopayRaw(missingSubscriptionReq, token);

            if (missingSubscriptionRes.has("success")
                    && !missingSubscriptionRes.get("success").isNull()) {
                apiAssertions.assertFieldTrue(
                        !missingSubscriptionRes.get("success").asBoolean(),
                        "success",
                        "API should fail when subscriptionType is missing");
            }
            if (missingSubscriptionRes.has("status")
                    && !missingSubscriptionRes.get("status").isNull()) {
                apiAssertions.assertFieldsEquals(
                        missingSubscriptionRes.get("status").asText(), "error", "status");
            }
            if (missingSubscriptionRes.has("errorMessage")
                    && !missingSubscriptionRes.get("errorMessage").isNull()) {
                apiAssertions.assertFieldsEquals(
                        missingSubscriptionRes.get("errorMessage").asText(),
                        "Bad Request",
                        "errorMessage");
            }

            // ----- Negative Case 5: Negative mandateAmount -----
            InitiateAutopayRequest negMandateReq =
                    InitiateAutopayRequest.buildRequest(
                            PROVIDER,
                            NEG_MANDATE_AMOUNT,
                            AUTH_WORKFLOW_TYPE,
                            PACKAGE_NAME,
                            PHONEPE_VERSION_CODE,
                            SUBSCRIPTION_TYPE,
                            SUBS_SETUP_TYPE,
                            MANDATE_SETUP_FROM,
                            "SUCCESS");
            JsonNode negMandateRes = autopayMethods.initiateAutopayRaw(negMandateReq, token);

            if (negMandateRes.has("success") && !negMandateRes.get("success").isNull()) {
                apiAssertions.assertFieldTrue(
                        !negMandateRes.get("success").asBoolean(),
                        "success",
                        "API should fail for negative mandateAmount");
            }
            if (negMandateRes.has("status") && !negMandateRes.get("status").isNull()) {
                apiAssertions.assertFieldsEquals(
                        negMandateRes.get("status").asText(), "error", "status");
            }

            // ----- Negative Case 6: Invalid mockServerTransactionStatus -----
            InitiateAutopayRequest invalidMockServerReq =
                    InitiateAutopayRequest.buildRequest(
                            PROVIDER,
                            MANDATE_AMOUNT,
                            AUTH_WORKFLOW_TYPE,
                            PACKAGE_NAME,
                            PHONEPE_VERSION_CODE,
                            SUBSCRIPTION_TYPE,
                            SUBS_SETUP_TYPE,
                            MANDATE_SETUP_FROM,
                            INVALID_STATUS);
            JsonNode invalidMockServerRes =
                    autopayMethods.initiateAutopayRaw(invalidMockServerReq, token);

            if (invalidMockServerRes.has("success")
                    && !invalidMockServerRes.get("success").isNull()) {
                apiAssertions.assertFieldTrue(
                        !invalidMockServerRes.get("success").asBoolean(),
                        "success",
                        "API should fail for invalid mockServerTransactionStatus");
            }
            if (invalidMockServerRes.has("status")
                    && !invalidMockServerRes.get("status").isNull()) {
                apiAssertions.assertFieldsEquals(
                        invalidMockServerRes.get("status").asText(), "error", "status");
            }

        } catch (Exception e) {
            log.error("Autopay Initiate Negative Test Failed: {}", e.getMessage(), e);
            softAssert.fail("Test Exception: " + e.getMessage());
        } finally {
            softAssert.assertAll();
        }
    }

    @Test(
            priority = 3,
            groups = {"status-autopay"},
            dependsOnGroups = {"initiate-autopay"},
            description = "Test Autopay Status API (skip DB validation if DB already updated)")
    public void testAutopayStatus(ITestContext context) {

        apiAssertions.assertFieldNotNull(token, "AUTH_TOKEN");
        MongoDBUtils mongoUtils = null;

        try {
            // 1. Build Status API request
            log.info(
                    "Building Status API request with subscriptionId={}, authReqId={}",
                    AutopayFlowContext.subscriptionId,
                    AutopayFlowContext.authReqId);
            // 1. Build Status API request
            StatusAutopayRequest statusRequest =
                    StatusAutopayRequest.build(
                            AutopayFlowContext.subscriptionId, // initiate _id
                            UPI_APP,
                            AutopayFlowContext.authReqId);

            // 2. Call Status API
            StatusAutopayResponse statusResponse =
                    autopayMethods.autopayStatus(statusRequest, token);

            log.info("Status API Response: {}", statusResponse);

            // 3. API Assertions
            apiAssertions.assertFieldTrue(
                    statusResponse.isSuccess(), "success", "Status API should return success=true");

            apiAssertions.assertFieldNotNull(statusResponse.getData(), "response.data");

            apiAssertions.assertFieldsEquals(
                    statusResponse.getData().getStatus(), "SUCCESS", "API status");

            // REAL DB subscriptionId (UUID)
            String dbSubscriptionId = statusResponse.getData().getSubscriptionId();

            apiAssertions.assertFieldNotNull(dbSubscriptionId, "subscriptionId from Status API");

            log.info("DB SubscriptionId: {}", dbSubscriptionId);

            // 4. DB Validation (skip if state ACTIVE)
            String mongoUri = DBConstants.CHANGEJAR_MONGO_DB_URL;

            mongoUtils = new MongoDBUtils(mongoUri);

            Document dbRecord =
                    mongoUtils.fetchData(
                            DB_CHANGEJAR,
                            COLLECTION_SUBSCRIPTIONS,
                            "subscriptionId",
                            dbSubscriptionId,
                            FIELD_UPDATED_AT);

            if (dbRecord != null) {
                log.info("DB Record FOUND");
                log.info(
                        "DB Record Snapshot: state={}, authWorkflowType={}, updatedAt={}",
                        dbRecord.getString("state"),
                        dbRecord.getString("authWorkflowType"),
                        dbRecord.getDate(FIELD_UPDATED_AT));

                String dbState = dbRecord.getString("state");
                String dbAuthWorkflow = dbRecord.getString("authWorkflowType");

                // Skip DB validation if state already ACTIVE
                if ("ACTIVE".equalsIgnoreCase(dbState)) {
                    log.info("DB validation skipped since state is already ACTIVE");

                    apiAssertions.assertFieldsEquals(
                            dbAuthWorkflow, "TRANSACTION", "authWorkflowType");

                } else {
                    log.info("DB state is NOT ACTIVE, performing strict DB assertions");
                    // Normal DB assertions if state is not ACTIVE
                    apiAssertions.assertFieldsEquals(dbState, "ACTIVE", "DB state");
                    apiAssertions.assertFieldsEquals(
                            dbAuthWorkflow, "TRANSACTION", "authWorkflowType");
                }
            } else {
                apiAssertions.assertFieldNotNull(
                        dbRecord, "DB record should exist after SUCCESS status");
            }

        } catch (Exception e) {
            apiAssertions.assertFieldTrue(
                    false, "Exception", "Autopay Status Test failed: " + e.getMessage());
        } finally {
            apiAssertions.assertAll();
        }
    }

    @Test(
            priority = 4,
            groups = {"status-autopay"},
            dependsOnGroups = {"initiate-autopay"},
            description = "Negative: Invalid authReqId should fail without DB update")
    public void testAutopayStatus_invalidAuthReqId(ITestContext context) {

        apiAssertions.assertFieldNotNull(token, "AUTH_TOKEN");

        try {
            // -------- Build Request --------
            StatusAutopayRequest request =
                    StatusAutopayRequest.build(
                            AutopayFlowContext.subscriptionId, UPI_APP, INVALID_AUTH_REQ_ID);

            // -------- Hit API --------
            StatusAutopayResponse response = autopayMethods.autopayStatus(request, token);
            log.info("Invalid authReqId response: {}", response);

            // -------- API Assertions --------
            apiAssertions.assertFieldTrue(
                    !response.isSuccess(),
                    "success",
                    "Autopay Status API should fail for invalid authReqId");

            // Data should be null when API fails
            apiAssertions.assertFieldTrue(
                    response.getData() == null,
                    "data",
                    "Data should be null for invalid authReqId");

            // -------- NO DB VALIDATION --------
            log.info("Skipping DB validation for invalid authReqId case");

        } catch (Exception e) {
            apiAssertions.assertFieldTrue(false, "Exception", "Test failed: " + e.getMessage());
        } finally {
            apiAssertions.assertAll();
        }
    }

    @Test(
            priority = 5,
            groups = {"status-autopay"},
            dependsOnGroups = {"initiate-autopay"},
            description = "Negative: Missing authReqId should fail without DB update")
    public void testAutopayStatus_missingAuthReqId(ITestContext context) {

        apiAssertions.assertFieldNotNull(token, "AUTH_TOKEN");

        try {
            // -------- Build Request (NO authReqId) --------
            StatusAutopayRequest request =
                    StatusAutopayRequest.build(
                            AutopayFlowContext.subscriptionId, UPI_APP, "" // missing authReqId
                            );

            // -------- Hit API --------
            StatusAutopayResponse response = autopayMethods.autopayStatus(request, token);

            log.info("Missing authReqId response: {}", response);

            // -------- API Assertions --------
            apiAssertions.assertFieldTrue(
                    !response.isSuccess(),
                    "success",
                    "Autopay Status API should fail for missing authReqId");

            // Negative case → backend returns data = null
            apiAssertions.assertFieldTrue(
                    response.getData() == null,
                    "data",
                    "Response data should be null for missing authReqId");

            // -------- DB VALIDATION SKIPPED INTENTIONALLY --------
            log.info("DB validation skipped for missing authReqId negative case");

        } catch (Exception e) {
            apiAssertions.assertFieldTrue(
                    false, "Exception", "Test failed due to exception: " + e.getMessage());
        } finally {
            apiAssertions.assertAll();
        }
    }
}
