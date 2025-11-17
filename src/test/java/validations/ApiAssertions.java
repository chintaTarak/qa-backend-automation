package validations;

import io.restassured.response.Response;
import org.testng.asserts.SoftAssert;

public class ApiAssertions {

    protected final SoftAssert softAssert;

    public ApiAssertions(SoftAssert softAssert) {
        this.softAssert = softAssert;
    }

    // Common check for 2xx success
    public void assertHttpSuccess(int statusCode, String apiName) {
        softAssert.assertTrue(statusCode >= 200 && statusCode < 300,
                apiName + " failed: Expected 2xx but got " + statusCode);
    }

    // Common check for non-2xx
    public void assertHttpFailure(Response response, String apiName) {
        int statusCode = response.getStatusCode();
        softAssert.assertTrue(statusCode >= 400,
                apiName + " expected failure but got success (" + statusCode + ")");
    }

    // Common check for success flag in JSON
    public void assertSuccessFlag(Response response, String apiName) {
        boolean success = response.jsonPath().getBoolean("success");
        softAssert.assertTrue(success,
                apiName + " failed: success flag is false or null");
    }

    // Common message check
    public void assertMessage(Response response, String expectedMessage, String apiName) {
        String actualMessage = response.jsonPath().getString("message");
        softAssert.assertEquals(actualMessage, expectedMessage,
                apiName + " failed: expected message '" + expectedMessage +
                        "' but got '" + actualMessage + "'");
    }

    // Common field check
    public void assertNotNullField(Object field, String fieldName) {
        softAssert.assertNotNull(field, fieldName + " should not be null");
    }

    // End of assertions
    public void assertAll() {
        softAssert.assertAll();
    }
}
