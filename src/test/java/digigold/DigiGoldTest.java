package digigold;

import static digigold.DigiGoldDataProvider.ExpectedError;
import static org.jarApiAutomation.dbConfiguration.DBConstants.*;
import static testData.digiGold.DigiGoldTestData.*;

import base.BaseTest;
import base.BuyCalculator;
import base.SellCalculator;
import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.jarApiAutomation.data.requestModel.digiGold.*;
import org.jarApiAutomation.data.responseModel.digiGold.*;
import org.jarApiAutomation.dbConfiguration.DataBaseFactory;
import org.testng.ITestContext;
import org.jarApiAutomation.data.requestModel.digiGold.CreateUserRequest;
import org.jarApiAutomation.data.requestModel.digiGold.SellConfirmRequest;
import org.jarApiAutomation.data.requestModel.digiGold.SellVerifyRequest;
import org.jarApiAutomation.data.responseModel.digiGold.*;
import org.jarApiAutomation.data.responseModel.digiGold.SellConfirmResponse;
import org.jarApiAutomation.data.responseModel.digiGold.SellPriceResponse;
import org.jarApiAutomation.data.responseModel.digiGold.SellVerifyResponse;
import org.jarApiAutomation.utils.CommonUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

@Slf4j
public class DigiGoldTest extends BaseTest {

    private final DigiGoldMethods digiGoldMethods = new DigiGoldMethods();
    SoftAssert softAssert = new SoftAssert();
    private DigiGoldValidation digiGoldValidation;
    BuyCalculator calculator = new BuyCalculator();
    SellCalculator sellCalculator = new SellCalculator();
    public String rateId;
    public String orderId;
    public BigDecimal assetPrice;
    public BigDecimal sellAssetPrice;
    public String invoiceId;

    @BeforeMethod
    public void setup() {
        softAssert = new SoftAssert();
        digiGoldValidation = new DigiGoldValidation(softAssert);
    }

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
            ITestContext context) {
        try {
            Map<String, String> headers =
                    "Without-Security-Header".equalsIgnoreCase(tenantInfo)
                            ? null
                            : Map.of("X-Tenant-Info", tenantInfo);
            BuyPriceResponse buyPriceResponse =
                    digiGoldMethods.buyPrice(Map.of("materialCode", materialCode), headers);
            if (expectedStatusCode == HttpStatus.SC_OK ) {
                String rateId = buyPriceResponse.getData().getId();
                assetPrice = buyPriceResponse.getData().getAssetPrice();
                context.setAttribute("rateId", rateId);
            }
            digiGoldValidation.assertBuyPriceResponse(
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
            Map<String, String> headers = "Without-Security-Header".equalsIgnoreCase(tenantInfo) ? null : Map.of("X-Tenant-Info", tenantInfo);
            BuyVerifyResponse buyVerifyResponse = digiGoldMethods.buyVerify(headers, buyVerifyRequest);
            digiGoldValidation.assertBuyVerifyResponse(buyVerifyResponse, expectedStatusCode,  expectedErrorCode,  expectedErrorMessage);
        }
        catch (Exception e)
        {
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
            Map<String, String> headers = "Without-Security-Header".equalsIgnoreCase(tenantInfo) ? null : Map.of("X-Tenant-Info", tenantInfo);
            BuyConfirmResponse  buyConfirmResponse = digiGoldMethods.buyConfirm(headers, buyConfirmRequest);
                digiGoldValidation.assertBuyConfirm(buyConfirmResponse, expectedError);
        }
        catch (Exception e)
        {
            log.error("Exception in Get buy confirm: ", e);
            softAssert.fail("Buy confirm test failed due to exception: " + e.getMessage());
        } finally {
            digiGoldValidation.assertAll();
        }
    }

    @Test(description = "Validate Buy Status API by fetching latest status",
            priority = 6, dataProvider = "buyStatusData", dataProviderClass = DigiGoldDataProvider.class)
    public void buyStatus(String orderId, String userId, String tenantInfo,ExpectedError expectedError,ITestContext context)
    {
        try {
            Map<String, String> headers = "Without-Security-Header".equalsIgnoreCase(tenantInfo) ? null : Map.of("X-Tenant-Info", tenantInfo);
            Map<String, String> queryParams = CommonUtil.buildQueryParams
                    ("orderId", orderId, "userId", userId);
           BuyStatusResponse buyStatusResponse = digiGoldMethods.buyStatus(queryParams, headers);
            if (buyStatusResponse.getStatusCode() == HttpStatus.SC_OK )
            {
                invoiceId = buyStatusResponse.getData().getInvoiceId();
                context.setAttribute("invoiceId", invoiceId);
                int expectedStatusCode = expectedError == null ? 200 : expectedError.getExpectedStatusCode();
                digiGoldValidation.assertBuyStatus(buyStatusResponse, expectedStatusCode);
            }

        } catch (Exception e) {
            log.error("Exception in get buy status: ", e);
            softAssert.fail("Buy status test failed due to exception: " + e.getMessage());
        } finally {
            digiGoldValidation.assertAll();
        }
    }

       @Test(description = "Check invoice details",dataProvider = "invoiceData",dataProviderClass = DigiGoldDataProvider.class,priority = 7)
       public void invoiceDetails( String invoiceId, String userId,String tenantInfo, String expectedErrorCode )
      {
         try
         {
             Map<String, String> headers = "Without-Security-Header".equalsIgnoreCase(tenantInfo) ? null : Map.of("X-Tenant-Info", tenantInfo);
             Map<String, String> queryParams = CommonUtil.buildQueryParams
                     ("invoiceId", invoiceId, "userId", userId);
             InvoiceResponse transactionsInvoices=digiGoldMethods.invoiceDetails(queryParams,headers);
             digiGoldValidation.assertInvoiceDetails(transactionsInvoices,expectedErrorCode);
         }
         catch (Exception e)
         {
             log.error("Exception in invoice details: ", e);
             softAssert.fail("Invoice  failed due to exception: " + e.getMessage());

         }
         finally
         {
             digiGoldValidation.assertAll();

         }

     }


    @Test(priority = 8, description = "verify Sell Price with Valid and Invalid Scenarios", dataProvider = "sellPriceScenarios", dataProviderClass = DigiGoldDataProvider.class)
    public void getSellPrice(String MATERIAL_CODE, String X_TENANT_INFO, int expectedStatusCode, ExpectedError expectedError, ITestContext context) {
        try {
            Map<String, String> headers =
                    X_TENANT_INFO == null ? null : Map.of("X-Tenant-Info", X_TENANT_INFO);
            Map<String, String> queryParams =
                    CommonUtil.buildQueryParams("materialCode", MATERIAL_CODE);
            SellPriceResponse sellPriceResponse =
                    digiGoldMethods.getSellPrice(headers, queryParams);
            if (sellPriceResponse.getStatusCode() == 200) {
                rateId = sellPriceResponse.getData().getId();
                sellAssetPrice = sellPriceResponse.getData().getAssetPrice();
                // Setting Rate ID Globally so that we can use from here to pass in Data Providers
                context.setAttribute("rateId", rateId);
            }
            // validate response based on status code
            digiGoldValidation.validateSellPrice(
                    sellPriceResponse, expectedStatusCode, expectedError);
        } catch (Exception e) {
            log.error("Exception during Sell Price: ", e);
            softAssert.fail("Get Sell Price test failed due to exception: " + e.getMessage());
        } finally {
            digiGoldValidation.assertAll();
        }
    }

    @Test(
            priority = 9,
            description = "verify Sell verify with Valid and Invalid Scenarios",
            dataProvider = "sellVerifyScenarios",
            dataProviderClass = DigiGoldDataProvider.class)
    public void sellVerify(
            SellVerifyRequest sellVerifyRequest,
            String X_TENANT_INFO,
            int expectedStatusCode,
            ExpectedError expectedError,
            ITestContext context) {
        try {
            String type = sellVerifyRequest.getCalculationType();
            if (CALCULATION_TYPE_AMOUNT.equals(type)) {
                sellVerifyRequest.setVolume(
                        sellCalculator
                                .sellByAmount(sellVerifyRequest.getAmount(), sellAssetPrice)
                                .getSixDecimal());
            } else {
                sellVerifyRequest.setAmount(
                        sellCalculator
                                .sellByQuantity(sellVerifyRequest.getVolume(), sellAssetPrice)
                                .getSixDecimal());
            }
            Map<String, String> headers =
                    X_TENANT_INFO == null ? null : Map.of("X-Tenant-Info", X_TENANT_INFO);
            SellVerifyResponse sellVerifyResponse =
                    digiGoldMethods.sellVerify(headers, sellVerifyRequest);
            if (sellVerifyResponse.getStatusCode() == 200 && sellVerifyResponse.getData() != null) {
                orderId = sellVerifyResponse.getData().getOrderId();
                // Setting Rate ID Globally so that we can use from here to pass in Data Providers
                context.setAttribute("orderId", orderId);
            }
            digiGoldValidation.validateSellVerify(
                    sellVerifyResponse, expectedStatusCode, expectedError);
        } catch (Exception e) {
            log.error("Exception during Sell Price: ", e);
            softAssert.fail("Sell Verify test failed due to exception: " + e.getMessage());
        } finally {
            softAssert.assertAll();
        }
    }

    @Test(
            priority = 10,
            description = "verify Sell Confirm with Valid and Invalid Scenarios",
            dataProvider = "sellConfirmScenarios",
            dataProviderClass = DigiGoldDataProvider.class)
    public void sellConfirm(
            SellConfirmRequest sellConfirmRequest,
            String X_TENANT_INFO,
            int expectedStatusCode,
            ExpectedError expectedError) {
        try {
            Map<String, String> headers =
                    X_TENANT_INFO == null ? null : Map.of("X-Tenant-Info", X_TENANT_INFO);
            SellConfirmResponse sellConfirmResponse =
                    digiGoldMethods.sellConfirm(headers, sellConfirmRequest);
            digiGoldValidation.validateSellConfirm(
                    sellConfirmResponse, expectedStatusCode, expectedError);
        } catch (Exception e) {
            log.error("Exception during Sell Confirm: ", e);
            softAssert.fail("Sell Confirm test failed due to exception: " + e.getMessage());
        } finally {
            softAssert.assertAll();
        }
    }

    @Test(
            priority = 11,
            description = "verify Sell User with Valid and Invalid Scenarios",
            dataProvider = "sellStatusScenarios",
            dataProviderClass = DigiGoldDataProvider.class)
    public void sellStatus(
            String ORDER_ID,
            String USER_ID,
            String X_TENANT_INFO,
            int expectedStatusCode,
            ExpectedError expectedError) {
        try {
            Map<String, String> headers =
                    X_TENANT_INFO == null ? null : Map.of("X-Tenant-Info", X_TENANT_INFO);
            Map<String, String> queryParams =
                    CommonUtil.buildQueryParams("orderId", ORDER_ID, "userId", USER_ID);
            SellStatusResponse sellStatusResponse =
                    digiGoldMethods.sellStatus(headers, queryParams);
            digiGoldValidation.validateSellStatus(
                    sellStatusResponse, expectedStatusCode, expectedError);
        } catch (Exception e) {
            log.error("Exception during Sell Status: ", e);
            softAssert.fail("Get Sell Status test failed due to exception: " + e.getMessage());
        } finally {
            softAssert.assertAll();
        }
    }

    @Test(
            description = "Validate All Delivery Products Data",
            priority = 12,
            dataProvider = "searchProduct",
            dataProviderClass = DigiGoldDataProvider.class)
    public void searchAllProduct(
            int pageNo,
            int pageSize,
            String X_TENANT_INFO,
            int expectedStatusCode,
            ExpectedError expectedError) {
        try {
            Map<String, String> headers =
                    X_TENANT_INFO == null ? null : Map.of("X-Tenant-Info", X_TENANT_INFO);
            Map<String, String> queryParams =
                    CommonUtil.buildQueryParams(
                            "pageNo", pageNo,
                            "pageSize", pageSize);
            JsonNode response = digiGoldMethods.getAllProduct(headers, queryParams);
            int statusCode = response.path("statusCode").asInt();
            digiGoldValidation.validateProducts(
                    response, statusCode, expectedStatusCode, expectedError);
        } catch (Exception e) {
            log.error("Exception in Get All Product: ", e);
            softAssert.fail("Get All Product test failed due to exception: " + e.getMessage());
        } finally {
            digiGoldValidation.assertAll();
        }
    }
    @Test(
            description = "Validate the Products Details",
            priority = 13,
            dataProvider = "getProductDetails",
            dataProviderClass = DigiGoldDataProvider.class)
    public void getProductDetails(
            String skuId,
            String X_TENANT_INFO,
            int expectedStatusCode,
            ExpectedError expectedError) {
        try {
            Map<String, String> headers =
                    X_TENANT_INFO == null ? null : Map.of("X-Tenant-Info", X_TENANT_INFO);
            Map<String, String> queryParams = CommonUtil.buildQueryParams("skuId", skuId);

            JsonNode response = digiGoldMethods.productDetails(headers, queryParams);
            int statusCode = response.path("statusCode").asInt();
            digiGoldValidation.validateProducts(
                    response, statusCode, expectedStatusCode, expectedError);
        } catch (Exception e) {
            log.error("Exception in Get Product Details: ", e);
            softAssert.fail("Get Product Details failed due to exception: " + e.getMessage());
        } finally {
            digiGoldValidation.assertAll();
        }
    }


    @Test(description = "Validate Delivery Order API", dataProvider = "deliveryOrderScenarios", dataProviderClass = DigiGoldDataProvider.class, priority = 14)
    public void deliveryOrder(DeliveryOrderRequest request, String tenantInfo, ExpectedError expectedError,ITestContext context)
    {
        Map<String, String> headers = "Without-Security-Header".equalsIgnoreCase(tenantInfo) ? null : Map.of("X-Tenant-Info", tenantInfo);

        try
        {
            DeliveryOrderResponse  deliveryOrderResponse = digiGoldMethods.deliveryOrder(headers,request );
            if (deliveryOrderResponse.getStatusCode() == HttpStatus.SC_OK )
            {
                orderId = deliveryOrderResponse.getData().getOrderId();
                context.setAttribute("orderId", orderId);
                digiGoldValidation.assertDeliveryOrder( deliveryOrderResponse,expectedError);
            }
        }
        catch (Exception e)
        {
            log.error("Exception in delivery order API: ", e);
            softAssert.fail("Delivery order failed due to exception: " + e.getMessage());
        }
        finally
        {
            digiGoldValidation.assertAll();
        }
    }
    @Test(description = "Confirm delivery Order ",priority = 15,dataProvider = "deliveryOrderConfirmData" ,dataProviderClass = DigiGoldDataProvider.class)
    public void deliveryOrderConfirm( DeliveryOrderConfirmRequest request,String tenantInfo,ExpectedError expectedError)
    {
        Map<String, String> headers = "Without-Security-Header".equalsIgnoreCase(tenantInfo) ? null : Map.of("X-Tenant-Info", tenantInfo);
        DeliveryOrderConfirmResponse deliveryOrderConfirmResponse =digiGoldMethods.deliveryOrderConfirm(headers,request);
        try {
            if (deliveryOrderConfirmResponse.getStatusCode() == HttpStatus.SC_OK && deliveryOrderConfirmResponse.getData()!=null)
            {
                digiGoldValidation.assertDeliveryOrderConfirmation(deliveryOrderConfirmResponse, expectedError);
            }
        }
        catch (Exception e)
        {
            log.error("Exception in delivery order API: ", e);
            softAssert.fail("Delivery order failed due to exception: " + e.getMessage());
        }
        finally
        {
            digiGoldValidation.assertAll();
        }
    }
    @Test(description = "Get order details ",dataProvider = "deliveryOrderFetchData",dataProviderClass = DigiGoldDataProvider.class,priority = 16)
    public void deliveryOrderDetails(String orderId, String userId, String tenantInfo ,ExpectedError expectedError)
    {
        try
        {
            Map<String, String> headers = "Without-Security-Header".equalsIgnoreCase(tenantInfo) ? null : Map.of("X-Tenant-Info", tenantInfo);

            Map<String, String> queryParams = CommonUtil.buildQueryParams
                    ("orderId", orderId, "userId", userId);

            DeliveryOrderResponse deliveryOrderResponse=digiGoldMethods.getOrderDeliveryDetails(headers,queryParams);
            digiGoldValidation.assertDeliveryOrderDetails( deliveryOrderResponse,expectedError);
        }
        catch (Exception e)
        {
            log.error("Exception in delivery order details API: ", e);
            softAssert.fail("Delivery order details failed due to exception: " + e.getMessage());
        }
        finally
        {
            digiGoldValidation.assertAll();
        }

    }

}



