package digigold;

import base.BaseTest;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.jarApiAutomation.data.requestModel.digiGold.CreateUserRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.Map;


@Slf4j
public class DigiGoldTest extends BaseTest {

    private final DigiGoldMethods digiGoldMethods = new DigiGoldMethods();
    SoftAssert softAssert = new SoftAssert();
    private DigiGoldValidation digiGoldValidation;

    @BeforeMethod
    public void setup() {
        softAssert = new SoftAssert();
        digiGoldValidation = new DigiGoldValidation(softAssert);
    }

    @Test(description = "Verify Create User API with valid and invalid scenarios", dataProvider = "userCreationScenarios", dataProviderClass = DigiGoldDataProvider.class)
    public void createUser(CreateUserRequest createUserRequest, String X_TENANT_INFO, int expectedStatusCode, String expectedErrorCode, String expectedErrorMessage) {
        try {
            Map<String, String> headers = "Without-Security-Header".equalsIgnoreCase(X_TENANT_INFO) ? null : Map.of("X-Tenant-Info", X_TENANT_INFO);

            Response createUserResponse = digiGoldMethods.createUser(headers, createUserRequest);

            //validate response based on status code
            digiGoldValidation.validateUserCreation(expectedStatusCode, X_TENANT_INFO, createUserRequest, createUserResponse, expectedErrorCode, expectedErrorMessage);

        } catch (Exception e) {
            log.error("Exception during Request OTP: ", e);
            softAssert.fail("Request OTP test failed due to exception: " + e.getMessage());
        } finally {
            digiGoldValidation.assertAll();
        }
    }


}