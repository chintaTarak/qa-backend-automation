package authService;
import testData.Auth.TestDataAuth;
import org.testng.annotations.DataProvider;

public class AuthDataProvider
{
    @DataProvider(name = "invalidPhoneNumbers")
    public static Object[][] invalidPhoneNumbers() {
        return new Object[][]{
                {"123456"},              // less than required digits
                {"987654321098"},        // more than required digits
                {"abcdefghij"},          // alphabetic
                {"98@76#54"},            // special characters
                {"0000000000"},          // repeated digits, invalid number
                {""},                    // empty value
                {" "},                   // space
                {"+91"},                 // only country code
                {"98765"},               // random short input
                {"12345abcde"},
                {TestDataAuth.PHONE_NUMBER} // Happy case
        };
    }
}
