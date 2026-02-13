package goldSDK;

import base.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.jarApiAutomation.data.requestModel.goldSDK.*;
import org.jarApiAutomation.data.requestModel.goldSDK.BaseKycRequest;
import org.jarApiAutomation.data.responseModel.goldSDK.*;
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

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
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
    private static Map<String, String> presignedUrlMap = new HashMap<>();
    private static Map<String, String> documentImageIdMap = new HashMap<>();

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
     * Upload Document API.
     * Generates presigned URL and documentImageId for given docType.
     * Values are stored in maps for PAN and AADHAAR flows.
     * Used later for file upload and KYC initiation.
     */
    @Test(priority = 6, description = "Upload document and get presigned URL", dataProvider = "uploadDocTypes", dataProviderClass = GoldSDKDataProvider.class)
    public void uploadDocument(String docType) {
        Map<String, String> headers = Map.of("Authorization", accessToken);
        UploadResponse response = goldSDKMethods.upload(headers);
        goldSDKValidation.validateUpload(response);
        // store separately
        presignedUrlMap.put(docType, response.getData().getPreSignedUrlPath());
        documentImageIdMap.put(docType, response.getData().getDocumentImageId());
    }

    /**
     * Upload File API.
     * Uploads PAN / AADHAAR image using previously generated presigned URL.
     * Reads test image from resources and validates upload status.
     * Depends on uploadDocument execution.
     */

    @Test(priority = 7, description = "Upload file using presigned URL", dataProvider = "uploadDocTypes", dataProviderClass = GoldSDKDataProvider.class, dependsOnMethods = "uploadDocument")
    public void uploadFile(String docType) throws Exception {
        String fileName = docType.equals("PAN") ? "testData/PanCard.jpeg" : "testData/AadhaarCard.jpeg";
        URL resource = getClass().getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new RuntimeException("File not found: " + fileName);
        }
        File imageFile = Paths.get(resource.toURI()).toFile();
        int statusCode = goldSDKMethods.uploadFile(presignedUrlMap.get(docType), imageFile, "image/jpeg");
        goldSDKValidation.validateUploadFile(statusCode);
    }

    /**
     * Initiate KYC API.
     * Triggers PAN or AADHAAR KYC based on request type.
     * Injects uploaded document imageId and validates API + DB status.
     * Depends on successful file upload.
     */
    @Test(priority = 8, description = "Initiate KYC", dataProvider = "initiateKycScenarios", dataProviderClass = GoldSDKDataProvider.class, dependsOnMethods = "uploadFile")
    public void initiateKyc(BaseKycRequest request, GoldSDKDataProvider.ExpectedError expectedError) {
        String imageId;
        String docType;
        InitiateKycResponse response;
        Map<String, String> headers = Map.of("Authorization", accessToken);
        try {
            if (accessToken == null) {
                softAssert.fail("Access token is null");
                return;
            }
            // -------- PAN FLOW --------
            if (request instanceof InitiatePanKycRequest panReq) {
                docType = panReq.getPanVerificationDoc().getKycDocType();
                imageId = documentImageIdMap.get(docType);
                panReq.getPanVerificationDoc().setDocFrontImageId(imageId);
                response = goldSDKMethods.initiateKyc(headers, panReq);
                goldSDKValidation.validateInitiateKyc(response, docType, expectedError);
            }
            // -------- AADHAAR FLOW --------
            else if (request instanceof InitiateAadhaarKycRequest aadhaarReq) {
                docType = aadhaarReq.getKycVerificationDoc().getKycDocType();
                imageId = documentImageIdMap.get(docType);
                aadhaarReq.getKycVerificationDoc().setDocFrontImageId(imageId);
                response = goldSDKMethods.initiateKyc(headers, aadhaarReq);
                goldSDKValidation.validateInitiateKyc(response, docType, expectedError);
            }
        } catch (Exception e) {
            log.error("Exception while initiating KYC", e);
            softAssert.fail("Initiate KYC failed: " + e.getMessage());
        } finally {
            goldSDKValidation.assertAll();
        }
    }

    /**
     * Fetch KYC Status API.
     * Retrieves verification status for uploaded PAN/AADHAAR documents.
     * Validates response fields and DB verification status.
     * Depends on successful KYC initiation.
     */
    @Test(priority = 10, description = "Fetch KYC status", dataProvider = "kycStatusScenarios", dataProviderClass = GoldSDKDataProvider.class, dependsOnMethods = "initiateKyc")
    public void getKycStatus(String docType, GoldSDKDataProvider.ExpectedError expectedError) {
        Map<String, String> headers = Map.of("Authorization", accessToken);
        try {
            if (accessToken == null) {
                softAssert.fail("Access token is null. Authentication might have failed.");
                return;
            }
            KycStatusResponse response = goldSDKMethods.getKycStatus(headers);
            goldSDKValidation.validateKycStatus(response, docType, expectedError);
        } catch (Exception e) {
            log.error("Exception while fetching KYC status", e);
            softAssert.fail("KYC Status test failed: " + e.getMessage());
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
