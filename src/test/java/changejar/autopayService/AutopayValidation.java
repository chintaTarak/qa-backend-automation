package changejar.autopayService;

import static testData.Autopay.TestDataAutopay.*;

import com.mongodb.client.MongoCollection;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.jarApiAutomation.data.responseModel.autopay.InitiateAutopayResponse;
import org.jarApiAutomation.dbConfiguration.DataBaseFactory;
import org.jarApiAutomation.utils.ApiAssertions;
import org.testng.asserts.SoftAssert;

@Slf4j
public class AutopayValidation extends ApiAssertions {

    private final SoftAssert softAssert;

    public AutopayValidation(SoftAssert softAssert) {
        super(softAssert);
        this.softAssert = softAssert;
    }

    // Fetch subscription by ID (SAFE)

    // Validate response for Initiate API
    public void assertInitiateAutopayResponse(
            InitiateAutopayResponse response, String expectedState) {
        assertHttpSuccess(response.getStatusCode(), "Initiate Autopay API");

        softAssert.assertTrue(response.isSuccess(), "API should return success=true");
        assertFieldNotNull(response.getData(), "Data should not be null");

        if ("SUCCESS".equalsIgnoreCase(expectedState)) {
            assertFieldNotNull(
                    response.getData().getId(), "Subscription ID must not be null for SUCCESS");
            assertFieldNotNull(
                    response.getData().getMockServer().getAuthReqId(),
                    "AuthReqId must not be null for SUCCESS");
            assertFieldNotNull(response.getData().getProvider(), "Provider must not be null");
        } else if ("PENDING".equalsIgnoreCase(expectedState)
                || "FAILED".equalsIgnoreCase(expectedState)) {

            assertFieldNotNull(
                    response.getData().getId(),
                    "Subscription ID must not be null for " + expectedState);
        }

        softAssert.assertAll();
    }

    // Validate MongoDB subscription entry safely for double/integer
    public void assertSubscriptionInDB(String userId, String expectedState, double mandateAmount) {

        MongoCollection<Document> subscriptionsCollection =
                DataBaseFactory.changeJarMongo().getCollection("changejar", "subscriptions");

        Document subscriptionDoc =
                subscriptionsCollection
                        .find(new Document("userId", userId))
                        .sort(new Document("createdAt", -1))
                        .first();

        assertFieldNotNull(subscriptionDoc, "Subscription document must exist in DB");

        // Validate state
        String state = subscriptionDoc.getString("state");
        softAssert.assertEquals(state, expectedState, "Subscription state mismatch in DB");

        // Safe mandateAmount
        Number dbAmountNum = subscriptionDoc.get("mandateAmount", Number.class);
        double dbMandateAmount = dbAmountNum != null ? dbAmountNum.doubleValue() : 0.0;
        softAssert.assertEquals(dbMandateAmount, mandateAmount, "Mandate amount mismatch in DB");

        // Success field
        Boolean success = subscriptionDoc.getBoolean("success");
        softAssert.assertTrue(success != null && success, "Success field in DB must be true");

        log.info("DB Subscription verification passed for userId: {}, state: {}", userId, state);

        softAssert.assertAll();
    }

    // DB basic field validation helper
    // ------------------------------
    public void validateSubscriptionDbBasicFields(
            Document dbRecord,
            String expectedUserId,
            double expectedMandateAmount,
            String expectedState) {

        assertFieldNotNull(dbRecord, "DB record");

        softAssert.assertEquals(
                dbRecord.getString("provider"), PROVIDER, "provider mismatch in DB");

        softAssert.assertEquals(
                dbRecord.getString("authWorkflowType"),
                AUTH_WORKFLOW_TYPE,
                "authWorkflowType mismatch in DB");

        softAssert.assertEquals(
                dbRecord.getString("subscriptionType"),
                SUBSCRIPTION_TYPE,
                "subscriptionType mismatch in DB");

        softAssert.assertEquals(
                dbRecord.getString("userId"), expectedUserId, "userId mismatch in DB");

        Number dbAmountNum = dbRecord.get("mandateAmount", Number.class);
        double dbMandateAmount = dbAmountNum != null ? dbAmountNum.doubleValue() : 0.0;

        softAssert.assertEquals(
                dbMandateAmount, expectedMandateAmount, "mandateAmount mismatch in DB");

        softAssert.assertEquals(dbRecord.getString("state"), expectedState, "state mismatch in DB");
    }
}
