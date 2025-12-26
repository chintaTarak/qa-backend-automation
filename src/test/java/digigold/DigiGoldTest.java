package digigold;

import static digigold.DigiGoldDataProvider.ExpectedError;
import static org.jarApiAutomation.dbConfiguration.DBConstants.*;
import static org.jarApiAutomation.dbConfiguration.DataBaseFactory.*;
import static testData.digiGold.DigiGoldTestData.*;

import base.BaseTest;
import base.BuyCalculator;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.jarApiAutomation.data.requestModel.digiGold.*;
import org.jarApiAutomation.data.responseModel.digiGold.*;
import org.jarApiAutomation.dbConfiguration.DataBaseFactory;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

@Slf4j
public class DigiGoldTest extends BaseTest {

    private final DigiGoldMethods digiGoldMethods = new DigiGoldMethods();
    SoftAssert softAssert = new SoftAssert();
    private DigiGoldValidation digiGoldValidation;
    BuyCalculator calculator = new BuyCalculator();

    @BeforeMethod
    public void setup() {
        softAssert = new SoftAssert();
        digiGoldValidation = new DigiGoldValidation(softAssert);
    }

    public BigDecimal assetPrice;

    @Test(
            description = "Verify Create User API with valid and invalid scenarios",
            dataProvider = "userCreationScenarios",
            dataProviderClass = DigiGoldDataProvider.class,
            priority = 1)
    public void createUser(
            CreateUserRequest createUserRequest,
            String X_TENANT_INFO,
            int expectedStatusCode,
            String expectedErrorCode,
            String expectedErrorMessage) {
        try {
            Map<String, String> headers =
                    "Without-Security-Header".equalsIgnoreCase(X_TENANT_INFO)
                            ? null
                            : Map.of("X-Tenant-Info", X_TENANT_INFO);

            Response createUserResponse = digiGoldMethods.createUser(headers, createUserRequest);

            // validate response based on status code
            digiGoldValidation.validateUserCreation(
                    expectedStatusCode,
                    X_TENANT_INFO,
                    createUserRequest,
                    createUserResponse,
                    expectedErrorCode,
                    expectedErrorMessage);

        } catch (Exception e) {
            log.error("Exception in Create User: ", e);
            softAssert.fail("create User test failed due to exception: " + e.getMessage());
        } finally {
            digiGoldValidation.assertAll();
        }
    }

    @Test(
            description = "Verify Get User API with valid and invalid scenarios",
            dataProvider = "getUserScenarios",
            dataProviderClass = DigiGoldDataProvider.class,
            priority = 2)
    public void getUser(
            String X_TENANT_INFO,
            Map<String, Object> queryParams,
            int expectedStatusCode,
            String expectedErrorCode,
            String expectedErrorMessage) {
        try {
            Map<String, String> headers =
                    "Without-Security-Header".equalsIgnoreCase(X_TENANT_INFO)
                            ? null
                            : Map.of("X-Tenant-Info", X_TENANT_INFO);

            Response getUserResponse = digiGoldMethods.getUser(headers, queryParams);

            // validate response based on status code
            digiGoldValidation.validateGetUsers(
                    getUserResponse,
                    X_TENANT_INFO,
                    expectedStatusCode,
                    expectedErrorCode,
                    expectedErrorMessage);

        } catch (Exception e) {
            log.error("Exception in Get User: ", e);
            softAssert.fail("Get User test failed due to exception: " + e.getMessage());
        } finally {
            digiGoldValidation.assertAll();
        }
    }

    @Test(
            description = "Fetches and validates the current buy price of the asset",
            dataProvider = "buyPriceDataScenarios",
            dataProviderClass = DigiGoldDataProvider.class,
            priority = 3)
    public void buyPrice(
            String materialCode,
            String tenantInfo,
            int expectedStatusCode,
            String expectedErrorCode,
            String expectedErrorMessage,
            boolean dbCheckRequired,
            ITestContext context) {
        try {
            Map<String, String> headers =
                    "Without-Security-Header".equalsIgnoreCase(tenantInfo)
                            ? null
                            : Map.of("X-Tenant-Info", tenantInfo);
            BuyPriceResponse buyPriceResponse =
                    digiGoldMethods.buyPrice(Map.of("materialCode", materialCode), headers);
            Document doc = null;
            if (expectedStatusCode == HttpStatus.SC_OK && dbCheckRequired) {
                String rateId = buyPriceResponse.getData().getId();
                assetPrice = buyPriceResponse.getData().getAssetPrice();
                doc =
                        DataBaseFactory.digiGoldMongo()
                                .fetchData(DIGI_GOLD_DB, MATERIAL_RATE, id, rateId, SORT_FIELD);
                context.setAttribute("rateId", rateId);
            }
            digiGoldValidation.assertBuyPriceResponse(
                    doc,
                    buyPriceResponse,
                    expectedStatusCode,
                    expectedErrorCode,
                    expectedErrorMessage);
        } catch (Exception e) {
            log.error("Exception in Get buyPrice: ", e);
            softAssert.fail("Get buy price test failed due to exception: " + e.getMessage());
        } finally {
            softAssert.assertAll();
        }
    }

    @Test(
            description = "Validate DigiGold Buy API",
            dataProvider = "buyVerifyScenarios",
            priority = 4,
            dataProviderClass = DigiGoldDataProvider.class)
    public void buyVerify(
            BuyVerifyRequest buyVerifyRequest,
            String tenantInfo,
            int expectedStatusCode,
            String expectedErrorCode,
            String expectedErrorMessage) {
        try {
            if (expectedStatusCode == HttpStatus.SC_OK) {
                String type = buyVerifyRequest.getCalculationType();
                if (CALCULATION_TYPE_AMOUNT.equals(type)) {
                    buyVerifyRequest.setVolume(
                            calculator
                                    .buyByAmount(buyVerifyRequest.getAmount(), assetPrice)
                                    .getSixDecimal());
                } else if (CALCULATION_TYPE_QUANTITY.equals(type)) {
                    buyVerifyRequest.setAmount(
                            calculator
                                    .buyByQuantity(buyVerifyRequest.getVolume(), assetPrice)
                                    .getSixDecimal());
                }
            }
            Map<String, String> headers =
                    "Without-Security-Header".equalsIgnoreCase(tenantInfo)
                            ? null
                            : Map.of("X-Tenant-Info", tenantInfo);
            BuyVerifyResponse buyVerifyResponse =
                    digiGoldMethods.buyVerify(headers, buyVerifyRequest);
            if (buyVerifyResponse != null && buyVerifyResponse.getData() != null) {
                String orderId = buyVerifyResponse.getData().getOrderId();
                Reporter.getCurrentTestResult().getTestContext().setAttribute("orderId", orderId);
            }
            digiGoldValidation.assertBuyVerifyResponse(
                    buyVerifyResponse, expectedStatusCode, expectedErrorCode, expectedErrorMessage);
        } catch (Exception e) {
            log.error("Exception in Get buy verify: ", e);
            softAssert.fail("Buy verify test failed due to exception: " + e.getMessage());
        } finally {
            digiGoldValidation.assertAll();
        }
    }

    @Test(
            description = "Validate Buy Confirm API with sync/async confirmation handling",
            priority = 5,
            dataProvider = "buyConfirmData",
            dataProviderClass = DigiGoldDataProvider.class)
    public void buyConfirm(
            BuyConfirmRequest buyConfirmRequest, String tenantInfo, ExpectedError expectedError) {
        try {
            Map<String, String> headers =
                    "Without-Security-Header".equalsIgnoreCase(tenantInfo)
                            ? null
                            : Map.of("X-Tenant-Info", tenantInfo);
            BuyConfirmResponse buyConfirmResponse =
                    digiGoldMethods.buyConfirm(headers, buyConfirmRequest);
            digiGoldValidation.assertBuyConfirm(buyConfirmResponse, expectedError);

        } catch (Exception e) {
            log.error("Exception in Get buy confirm: ", e);
            softAssert.fail("Buy confirm test failed due to exception: " + e.getMessage());
        } finally {
            digiGoldValidation.assertAll();
        }
    }

    @Test(
            description = "Validate Buy Status API by fetching latest status",
            priority = 6,
            dataProvider = "buyStatusData",
            dataProviderClass = DigiGoldDataProvider.class)
    public void buyStatus(
            String orderId, String userId, String tenantInfo, ExpectedError expectedError) {
        try {
            Map<String, String> headers =
                    "Without-Security-Header".equalsIgnoreCase(tenantInfo)
                            ? null
                            : Map.of("X-Tenant-Info", tenantInfo);
            Map<String, String> queryParams = new HashMap<>();
            if (orderId != null && !orderId.isBlank()) {
                queryParams.put("orderId", orderId);
            }
            if (userId != null && !userId.isBlank()) {
                queryParams.put("userId", userId);
            }
            if (queryParams.isEmpty()) {
                queryParams = null;
            }
            BuyStatusResponse buyStatusResponse = digiGoldMethods.buyStatus(queryParams, headers);
            int expectedStatusCode =
                    expectedError == null ? 200 : expectedError.getExpectedStatusCode();
            digiGoldValidation.assertBuyStatus(buyStatusResponse, expectedStatusCode);

        } catch (Exception e) {
            log.error("Exception in get buy status: ", e);
            softAssert.fail("Buy status test failed due to exception: " + e.getMessage());
        } finally {
            digiGoldValidation.assertAll();
        }
    }
}
