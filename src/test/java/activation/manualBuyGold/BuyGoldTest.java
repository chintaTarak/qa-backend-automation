package activation.manualBuyGold;

import base.BuyCalculator;
import base.CalculationResult;
import changejar.authService.AuthServiceTest;

import java.math.BigDecimal;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jarApiAutomation.data.requestModel.activation.BuyGoldManualRequest;
import org.jarApiAutomation.data.requestModel.activation.PaymentStatusRequest;
import org.jarApiAutomation.data.responseModel.auth.responseBuyGold.BuyGoldResponse;
import org.jarApiAutomation.data.responseModel.auth.responseBuyGold.GetLivePriceResponse;
import org.jarApiAutomation.data.responseModel.auth.responseBuyGold.PaymentStatusResponse;
import org.jarApiAutomation.utils.CommonUtil;
import org.testng.annotations.Test;

@Slf4j
public class BuyGoldTest extends AuthServiceTest {


    private String rateId;
    private String ratevalidity;
    private String orderId;
    private double price;
    private BigDecimal priceBD;
    private BigDecimal amountBD;
    private BigDecimal volume;
    private double amount = 1000;
    int validity;
    String paymentStatus;
    BuyCalculator volumeCalculator=new BuyCalculator();
    private final BuyGoldManualValidation buyGoldValidation =
            new BuyGoldManualValidation(softAssert);

    @Test(
            priority = 10,
            description = "To fetch the live gold price",
            dataProvider = "getLiveGoldPriceScenarios",
            dataProviderClass = BuyGoldDataprovider.class)
    public void getLiveGoldPrice(
            String type,
            String accessToken,
            int expectedStatusCode,
            String expectedErrorCode,
            String expectedErrorMessage) {
        try {

            Map<String, String> headers =
                    accessToken == null ? null : Map.of("Authorization", "Bearer " +accessToken);
            Map<String, String> queryParams = CommonUtil.buildQueryParams("type", type);
            GetLivePriceResponse getPrice =
                    BuyGoldMethods.getLiveGoldPrice(headers, queryParams);
            if (getPrice.getData()!=null && expectedStatusCode== HttpStatus.SC_OK)
            {
                buyGoldValidation.getLivePriceValidation(getPrice);
                price = getPrice.getData().getPrice();
                priceBD = BigDecimal.valueOf(price);

              log.info("Price=" + price + ", priceBD=" + priceBD);
                rateId = getPrice.getData().getRateId();
                ratevalidity = getPrice.getData().getRateValidity();
                validity = getPrice.getData().getValidity();

            } else {
                softAssert.assertFalse(
                        getPrice.isSuccess(), "Expected failure but API returned success");
                softAssert.assertEquals(
                        getPrice.getErrorMessage(), expectedErrorMessage, "Error message mismatch");
                softAssert.assertNull(getPrice.getData(), "Data must be null for error response");
            }
        } catch (Exception e) {
            log.error("Failed to fetch live gold price", e);
            softAssert.fail(
                    "Fetch live gold price test failed due to exception: " + e.getMessage());
        } finally {
            softAssert.assertAll();
        }
    }

    @Test(
            priority = 11,
            description = "Manual Buy Gold – SUCCESS / PENDING / FAILURE",
            dataProvider = "manualGoldBuyTxnScenarios",
            dataProviderClass = BuyGoldDataprovider.class)
    public void buyGoldManual(

            String requestType,
            String accessToken,
            String mockServerTransactionStatus,
            int expectedStatusCode,
            String expectedErrorCode,
            String expectedErrorMessage) {
        try {
            amountBD = BigDecimal.valueOf(amount);

            if (priceBD == null || priceBD.compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalStateException("priceBD is not initialized or zero");
            }
            CalculationResult result =
                    volumeCalculator.buyByAmount(amountBD, priceBD);
            volume = result.getFourDecimal();
            BuyGoldManualRequest buyGoldRequest =
                    BuyGoldManualRequest.BugGoldManualRequestPayload(
                            amountBD,
                            volume,
                            priceBD,
                            rateId,
                            ratevalidity,
                            validity,
                            mockServerTransactionStatus);
            Map<String, String> headers =
                    accessToken == null ? null : Map.of("Authorization", "Bearer " +accessToken);
            BuyGoldResponse response =
                    BuyGoldMethods.postBuyGoldManual(buyGoldRequest, headers);
            orderId = response.getData().getOrderId();
            paymentStatus = response.getData().getMockServerManualPayment().getPaymentStatus();

            // -------- SUCCESS VALIDATION --------
            if ("SUCCESS".equalsIgnoreCase(mockServerTransactionStatus)) {
                buyGoldValidation.goldBuyManualvalidation(
                        response, AuthServiceTest.userId, rateId, amount, volume);
            }
            // -------- PAYMENT STATUS API (SUCCESS / PENDING / FAILURE) --------
            if ("SUCCESS".equalsIgnoreCase(mockServerTransactionStatus)
                    || "PENDING".equalsIgnoreCase(mockServerTransactionStatus)
                    || "FAILURE".equalsIgnoreCase(mockServerTransactionStatus)) {
                PaymentStatusRequest paymentStatusRequest =
                        PaymentStatusRequest.PaymentStatusRequestPayload(
                                "MOCK_SERVER", orderId, "GOLD_PURCHASE", "QUICK_ACTIONS");
                PaymentStatusResponse paymentStatusResponse =
                        BuyGoldMethods.postPaymentStatus(paymentStatusRequest, accessToken);

                // -------- PAYMENT STATUS VALIDATION --------
                if ("FAILURE".equalsIgnoreCase(mockServerTransactionStatus)) {
                    buyGoldValidation.buyGoldPaymentStatusValidation(
                            paymentStatusResponse, AuthServiceTest.userId, orderId, paymentStatus);
                } else {
                    buyGoldValidation.buyGoldPaymentStatusValidation(
                            paymentStatusResponse, AuthServiceTest.userId, orderId, paymentStatus);
                }
            }
        } catch (Exception e) {
            log.error(
                    "buy gold manual flow failed for state {}: {}",
                    mockServerTransactionStatus,
                    e.getMessage());
            softAssert.fail("Validation failed in buy gold manual flow : " + e.getMessage());
        } finally {
            softAssert.assertAll();
        }
    }
}
