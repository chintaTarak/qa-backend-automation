package activation.manualBuyGold;

import static activation.manualBuyGold.BuyGoldDataprovider.ExpectedError.*;
import static changejar.authService.AuthServiceTest.accessToken;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.testng.annotations.DataProvider;

public class BuyGoldDataprovider {
    @AllArgsConstructor
    @Getter
    public enum ExpectedError {
        ACCESS_DENIED(null, "Full authentication is required to access this resource", 200),

        BAD_REQUEST(null, "Something went wrong", 200),

        GENERIC_ERROR(null, "Something went wrong", 200);

        private final String errorCode;
        private final String errorMessage;
        private final int expectedStatusCode;
    }

    @DataProvider(name = "getLiveGoldPriceScenarios")
    public Object[][] getLiveGoldPriceScenarios() {
        return new Object[][] {
            {"BUY", accessToken, 200, null, null},
            {
                "BUY",
                "",
                ACCESS_DENIED.getExpectedStatusCode(),
                ACCESS_DENIED.getErrorCode(),
                ACCESS_DENIED.getErrorMessage()
            },
            {
                "INVALID",
                accessToken,
                BAD_REQUEST.getExpectedStatusCode(),
                BAD_REQUEST.getErrorCode(),
                BAD_REQUEST.getErrorMessage()
            }
        };
    }

    @DataProvider(name = "manualGoldBuyTxnScenarios")
    public Object[][] manualGoldBuyTxnScenarios() {
        return new Object[][] {
            {"BUY", accessToken, "PENDING", 200, null, null},
            {"BUY", accessToken, "FAILURE", 200, "PAYMENT_FAILED", "Payment failed"},
                {"BUY", accessToken, "SUCCESS", 200, null, null}
        };
    }
}
