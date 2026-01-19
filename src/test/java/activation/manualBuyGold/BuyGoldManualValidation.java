package activation.manualBuyGold;

import static org.jarApiAutomation.dbConfiguration.DataBaseFactory.changeJarMongo;
import static org.jarApiAutomation.utils.CommonUtil.getDoubleValueFromDocument;
import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import org.bson.Document;
import org.jarApiAutomation.data.responseModel.auth.responseBuyGold.BuyGoldResponse;
import org.jarApiAutomation.data.responseModel.auth.responseBuyGold.GetLivePriceResponse;
import org.jarApiAutomation.data.responseModel.auth.responseBuyGold.PaymentStatusResponse;
import org.jarApiAutomation.utils.ApiAssertions;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

public class BuyGoldManualValidation extends ApiAssertions {

    public BuyGoldManualValidation(SoftAssert softAssert) {
        super(softAssert);
    }


    public void getLivePriceValidation(GetLivePriceResponse response) {

        // Response object validation
        assertFieldNotNull(response, "Response object");
        if (!response.isSuccess()) {
            assertFieldTrue(
                    response.getData() == null, "Data", "Data must be null when success = false");
            assertAll();
            return;
        }

        GetLivePriceResponse.DataNode data = response.getData();
        assertFieldNotNull(data, "Data");
        assertFieldTrue(data.getPrice() > 0, "Price", "Price must be greater than 0");
        assertFieldTrue(data.getValidity() > 0, "Validity", "Validity must be greater than 0");
        assertFieldTrue(
                data.getApplicableTax() >= 0, "ApplicableTax", "ApplicableTax must be >= 0");
        assertFieldNotNull(data.getRateId(), "RateId");
        assertFieldFalse(data.getRateId().trim().isEmpty(), "RateId", "RateId must not be empty");
        assertFieldNotNull(data.getRateValidity(), "RateValidity");
        assertFieldFalse(
                data.getRateValidity().trim().isEmpty(),
                "RateValidity",
                "RateValidity must not be empty");
        assertFieldNotNull(data.getVendorType(), "VendorType");
        assertFieldFalse(
                data.getVendorType().trim().isEmpty(),
                "VendorType",
                "VendorType must not be empty");
        assertFieldTrue(
                data.isPriceDrop() == true || data.isPriceDrop() == false,
                "isPriceDrop",
                "isPriceDrop must be a valid boolean");
        assertFieldNotNull(data.getTaxItems(), "TaxItems");
        assertFieldTrue(
                !data.getTaxItems().isEmpty(), "TaxItems", "TaxItems list must not be empty");
        for (GetLivePriceResponse.TaxItem item : data.getTaxItems()) {
            assertFieldNotNull(item.getName(), "TaxItem.name");
            assertFieldFalse(
                    item.getName().trim().isEmpty(),
                    "TaxItem.name",
                    "TaxItem name must not be empty");
            assertFieldTrue(item.getValue() >= 0, "TaxItem.value", "TaxItem value must be >= 0");
            assertFieldNotNull(item.getType(), "TaxItem.type");
            assertFieldFalse(
                    item.getType().trim().isEmpty(),
                    "TaxItem.type",
                    "TaxItem type must not be empty");
            assertFieldsEquals(item.getType(), "PERCENTAGE", "TaxItem.type");
        }

        assertAll();
    }

    public void goldBuyManualvalidation(
            BuyGoldResponse response,
            String userId,
            String rateId,
            double expectedAmount,
            BigDecimal expectedVolume) {

        Document doc =
                changeJarMongo()
                        .fetchDataMultiFilter(
                                "changejar",
                                "goldTransactions",
                                Map.of("userId", userId, "rateId", rateId),
                                "_id");
        if (doc == null) {
            throw new AssertionError(
                    "No gold transaction found for userId=" + userId + ", rateId=" + rateId);
        }
        String orderId = doc.getString("orderId");
        double volume = getDoubleValueFromDocument(doc, "volume");
        double amount = getDoubleValueFromDocument(doc, "amount");

        assertAmountEquals(amount, expectedAmount);
        assertVolumeEquals(BigDecimal.valueOf(volume), expectedVolume);
        assertFieldsEquals(response.getData().getOrderId(), orderId, "orderId");
    }

    private void assertAmountEquals(double actual, double expected) {
        BigDecimal act = BigDecimal.valueOf(actual).setScale(2, RoundingMode.HALF_UP);
        BigDecimal exp = BigDecimal.valueOf(expected).setScale(2, RoundingMode.HALF_UP);

        if (act.compareTo(exp) != 0) {
            throw new AssertionError("Amount mismatch. Actual=" + act + ", Expected=" + exp);
        }
    }

    private void assertVolumeEquals(BigDecimal actual, BigDecimal expected) {
        if (actual == null || expected == null) {
            throw new AssertionError(
                    "Volume comparison failed. Actual=" + actual + ", Expected=" + expected
            );
        }

        BigDecimal act = actual.setScale(4, RoundingMode.HALF_UP);
        BigDecimal exp = expected.setScale(4, RoundingMode.HALF_UP);

        if (act.compareTo(exp) != 0) {
            throw new AssertionError(
                    "Volume mismatch. Actual=" + act + ", Expected=" + exp
            );
        }
    }


    public void buyGoldPaymentStatusValidation(
            PaymentStatusResponse response, String userId, String orderId, String expectedStatus) {

        Document doc =
                changeJarMongo()
                        .fetchDataMultiFilter(
                                "changejar",
                                "payments",
                                Map.of("userId", userId, "orderId", orderId),
                                "_id");

        Assert.assertNotNull(doc, "Payment record not found for orderId=" + orderId);

        // DB vs Expected
        assertEquals(doc.getString("txnStatus"), expectedStatus, "txnStatus mismatch");

        // DB vs API response
        if ("SUCCESS".equals(expectedStatus)) {
            assertEquals(
                    doc.getString("orderId"),
                    response.getData().getOrderId(),
                    "orderId mismatch for SUCCESS");
        } else {
            Assert.assertNull(
                    response.getData().getOrderId(),
                    "orderId should be null for " + expectedStatus);
        }

        assertEquals(
                doc.getString("paymentProvider"),
                response.getData().getPaymentProvider(),
                "paymentProvider mismatch");

        // Conditional validation using lambda-style logic
        switch (expectedStatus) {
            case "SUCCESS" -> {
                Assert.assertFalse(
                        response.getData().isRetryAllowed(),
                        "Retry should be disabled for SUCCESS");
            }

            case "FAILURE", "PENDING" -> {
                // retryAllowed is not part of response for these statuses
                // So we should NOT validate it
                System.out.println("Skipping retryAllowed validation for " + expectedStatus);
            }

            default -> throw new AssertionError("Unsupported payment status: " + expectedStatus);
        }
    }
}
