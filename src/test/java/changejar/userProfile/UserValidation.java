package changejar.userProfile;

import org.bson.Document;
import org.jarApiAutomation.data.responseModel.userProfile.UserDetailsResponse;
import org.jarApiAutomation.utils.ApiAssertions;
import org.testng.asserts.SoftAssert;

import static changejar.ApiErrorCodes.USER_LOGGED_OUT;
import static org.jarApiAutomation.dbConfiguration.DataBaseFactory.changeJarMongo;
import static org.jarApiAutomation.utils.CommonUtil.getValueFromDocument;

public class UserValidation extends ApiAssertions {

    private static final String mongoIdRegEx = "^[a-fA-F0-9]{24}$";

    public UserValidation(SoftAssert softAssert) {
        super(softAssert);
    }

    public void assertUserDetailsSuccessResponse(UserDetailsResponse userDetailsResponse) {
        assertHttpSuccess(userDetailsResponse.getStatusCode(), "User Details");
        assertFieldTrue(userDetailsResponse.isSuccess(), "success", "Expected success=true");
        assertFieldNotNull(userDetailsResponse.getData(), "Data must not be null on success");
        assertFieldNotNull(userDetailsResponse.getData().getUserId(), "User Id must not be null");
        // Fetch the document from MongoDB
        Document doc = changeJarMongo().fetchData("changejar", "users", "_id", userDetailsResponse.getData().getUserId(), "_id");
        String phoneNumber = getValueFromDocument(doc, "phoneNumber");
        assertFieldFalse(userDetailsResponse.getData().isOnboarded(), "onboarded", "User should be onboarded");
        assertFieldsEquals(userDetailsResponse.getData().getPhoneNumber(), phoneNumber, "Phone Number");
        assertFieldTrue(userDetailsResponse.getData().getUserId().matches(mongoIdRegEx), "userId", "Id does not match MongoDB Format");
    }

    public void assertUserDetailsFailureResponse(UserDetailsResponse userDetailsResponse, String expectedErrorCode, String expectedErrorMessage) {
        assertHttpFailure(userDetailsResponse.getStatusCode(), "User Details");
        assertFieldsEquals(userDetailsResponse.isSuccess(), false, "Success flag");
        assertFieldsEquals(userDetailsResponse.getErrorCode(), expectedErrorCode, "Error Code");
        assertFieldsEquals(userDetailsResponse.getErrorMessage(), expectedErrorMessage, "Error Message");
        assertFieldNotNull(userDetailsResponse.getData(), "Data should be null on failure");
    }
}
