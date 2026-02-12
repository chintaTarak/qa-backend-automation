package goldSDK;

import base.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jarApiAutomation.data.requestModel.goldSDK.AutoPayInitiateRequest;
import org.jarApiAutomation.data.requestModel.goldSDK.CreateUserRequest;
import org.jarApiAutomation.data.requestModel.goldSDK.RefreshTokenRequest;
import org.jarApiAutomation.data.responseModel.digiGold.BuyPriceResponse;
import org.jarApiAutomation.data.responseModel.goldSDK.AutoPayInitiateResponse;
import org.jarApiAutomation.data.responseModel.goldSDK.CreateUserResponse;
import org.jarApiAutomation.data.responseModel.goldSDK.GetUserResponse;
import org.jarApiAutomation.data.responseModel.goldSDK.UserAuthResponse;
import org.testng.ITestContext;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Gold SDK – User Authentication & User APIs Flow covered: 1. Create User (S2S) 2. Authenticate
 * User (Login) 3. Fetch User Details 4. Refresh Access Token
 */
@Slf4j
public class GoldSDKTest extends BaseTest {
    private final GoldSDKMethods goldSDKMethods = new goldSDK.GoldSDKMethods();
    SoftAssert softAssert = new SoftAssert();
    private GoldSDKValidation goldSDKValidation;
    public static String accessToken;
    public static String refreshToken;
    public static String userId;
    public static String frequency;
    public static String userRefId;
    public String rateId;
    public BigDecimal assetPrice;

    @BeforeMethod
    public void setup() {
        softAssert = new SoftAssert();
        goldSDKValidation = new GoldSDKValidation(softAssert);
    }

    @Parameters("savingsType")
    @BeforeClass
    public void beforeClassSetup(@Optional("DAILY") String savingsType) {
        frequency = savingsType;
    }

    @Test(
            priority = 1,
            description = "Create user via S2S Create User API",
            dataProvider = "userCreationScenarios",
            dataProviderClass = GoldSDKDataProvider.class)
    public void createUser(
            CreateUserRequest createUserRequest,
            String xApiKey,
            GoldSDKDataProvider.ExpectedError expectedError) {
        try {
            Map<String, String> headers = xApiKey != null ? Map.of("x-api-key", xApiKey) : Map.of();
            CreateUserResponse response = goldSDKMethods.createUser(headers, createUserRequest);
            // Capture userId only on SUCCESS
            if (expectedError == null && response.getData() != null) {
                userRefId = createUserRequest.getUserRefId();
                userId = response.getData().getId();
            }
            goldSDKValidation.validateUserCreation(createUserRequest, response, expectedError);
        } catch (Exception e) {
            log.error("Exception while creating user", e);
            softAssert.fail("Create User test failed: " + e.getMessage());
        } finally {
            goldSDKValidation.assertAll();
        }
    }

    /**
     * Authenticate user using Login API. On success, accessToken and refreshToken are captured for
     * subsequent API calls.
     */
    @Test(
            priority = 2,
            description = "Authenticate user and generate access & refresh tokens",
            dataProvider = "userAuthDetails",
            dataProviderClass = GoldSDKDataProvider.class)
    public void authenticateUser(
            String xApiKey,
            Map<String, Object> queryParams,
            GoldSDKDataProvider.ExpectedError expectedError) {
        try {
            Map<String, String> headers = xApiKey != null ? Map.of("x-api-key", xApiKey) : Map.of();
            UserAuthResponse response = goldSDKMethods.userAuth(headers, queryParams);
            // Capture tokens only on SUCCESS
            if (expectedError == null && response.getData() != null) {
                accessToken = response.getData().getAccessToken();
                refreshToken = response.getData().getRefreshToken();
            }
            goldSDKValidation.validateUserAuth(response, expectedError);
        } catch (Exception e) {
            log.error("Exception during user authentication", e);
            softAssert.fail("User authentication test failed: " + e.getMessage());
        } finally {
            goldSDKValidation.assertAll();
        }
    }

    /**
     * Fetch user details using access token. This test depends on successful authentication.
     */
    @Test(
            priority = 3,
            description = "Fetch user details using access token",
            dataProvider = "getUserScenarios",
            dataProviderClass = GoldSDKDataProvider.class,
            dependsOnMethods = "authenticateUser")
    public void fetchUserDetails(
            Map<String, Object> queryParams, GoldSDKDataProvider.ExpectedError expectedError) {
        try {
            if (accessToken == null) {
                softAssert.fail("Access token is null. Authentication might have failed.");
                return;
            }
            GetUserResponse response =
                    goldSDKMethods.getUser(Map.of("Authorization", accessToken), queryParams);
            goldSDKValidation.validateGetUsers(response, expectedError);
        } catch (Exception e) {
            log.error("Exception while fetching user details", e);
            softAssert.fail("Get User test failed: " + e.getMessage());
        } finally {
            goldSDKValidation.assertAll();
        }
    }

    /**
     * Refresh access token using refresh token. Depends on successful authentication.
     */
    @Test(
            priority = 4,
            description = "Refresh access token using refresh token",
            dataProvider = "getRefreshToken",
            dataProviderClass = GoldSDKDataProvider.class)
    public void refreshToken(
            RefreshTokenRequest refreshTokenRequest,
            GoldSDKDataProvider.ExpectedError expectedError) {
        try {
            UserAuthResponse response = goldSDKMethods.refreshToken(refreshTokenRequest);
            goldSDKValidation.validateRefreshToken(response, expectedError);
        } catch (Exception e) {
            log.error("Exception while refreshing access token", e);
            softAssert.fail("Refresh Token test failed: " + e.getMessage());
        } finally {
            goldSDKValidation.assertAll();
        }
    }


    @Test(
            description = "Initiate Auto Pay and validate response",
            dataProvider = "initiateAutoPay",
            dataProviderClass = GoldSDKDataProvider.class,
            dependsOnMethods = "authenticateUser"
    )
    public void initiateAutoPay(
            AutoPayInitiateRequest autoPayInitiateRequest, Map<String, String> headers, GoldSDKDataProvider.ExpectedError expectedError) {
        try {
            AutoPayInitiateResponse autoPayInitiateResponse = goldSDKMethods.initiateAutoPay(autoPayInitiateRequest, headers);
            goldSDKValidation.validateAutoPayInitiateResponse(autoPayInitiateResponse, expectedError);
        } catch (Exception e) {
            log.error("Exception while refreshing access token", e);
            softAssert.fail(" initiateAutoPay Test failed: " + e.getMessage());
        } finally {
            goldSDKValidation.assertAll();
        }
    }
    /**
     * Fetch  buy price for SDK . Depends on successful authentication.
     */

    @Test(description = "Fetch Gold SDK buy Price",dataProvider = "getBuyPriceSDKScenarios",dataProviderClass = GoldSDKDataProvider.class,priority = 5)
    public void fetchSDKBuyGoldPrice(String accessToken, GoldSDKDataProvider.ExpectedError expectedError, ITestContext context)
    {
        try
        {
            Map<String, String> headers = "Without-Security-Header".equalsIgnoreCase(accessToken) ? null
                            : Map.of("Authorization", "Bearer "+ accessToken);
            BuyPriceResponse buyPriceSDKResponse= goldSDKMethods.sdkBuyPrice(headers);
            if (buyPriceSDKResponse.getData()!=null )
            {
                String rateId = buyPriceSDKResponse.getData().getId();
                assetPrice = buyPriceSDKResponse.getData().getAssetPrice();
                context.setAttribute("rateId", rateId);
            }
            goldSDKValidation.assertSDKBuyPrice(buyPriceSDKResponse,expectedError);

        }
        catch (Exception e)
        {
            log.error("Exception while fetching user details", e);
            softAssert.fail("Get User test failed: " + e.getMessage());
        }
        finally
        {
            goldSDKValidation.assertAll();
        }
    }

}
