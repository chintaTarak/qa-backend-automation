package changejar.autopayService;

import org.testng.annotations.DataProvider;
import testData.Autopay.TestDataAutopay;

public class AutopayDataProvider {

    @DataProvider(name = "autopayStates")
    public Object[][] autopayStates() {
        return new Object[][] {
            // {mandateAmount, mockServerTransactionStatus, expectedState}
            {TestDataAutopay.SUCCESS_AMOUNT, "SUCCESS", TestDataAutopay.SUCCESS_STATE},
            {TestDataAutopay.PENDING_AMOUNT, "PENDING", TestDataAutopay.PENDING_STATE},
            {TestDataAutopay.FAILED_AMOUNT, "FAILED", TestDataAutopay.FAILED_STATE}
        };
    }
}
