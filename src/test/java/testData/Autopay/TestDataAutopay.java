package testData.Autopay;

public class TestDataAutopay {

    // Mandate amounts for different states
    public static final double SUCCESS_AMOUNT = 11.0;
    public static final double PENDING_AMOUNT = 112.0;
    public static final double FAILED_AMOUNT = 113.0;

    // Expected states
    public static final String SUCCESS_STATE = "CREATED";
    public static final String PENDING_STATE = "PENDING";
    public static final String FAILED_STATE = "FAILED";
    public static final String INVALID_STATUS = "INVALID_STATUS";

    // Provider info
    public static final String PROVIDER = "MOCK_SERVER";
    public static final String INVALID_PROVIDER = "INVALID_PROVIDER";

    public static final String INVALID_AUTH_REQ_ID = "invalid-authReq-123";
    public static final double INVALID_AMOUNT = 5.0;
    public static final double MANDATE_AMOUNT = 50.0;
    public static final double NEG_MANDATE_AMOUNT = -50.0;

    // Other request defaults
    public static final String AUTH_WORKFLOW_TYPE = "TRANSACTION";
    public static final String SUBSCRIPTION_TYPE = "DAILY_SAVINGS";
    public static final String SUBS_SETUP_TYPE = "SETUP";
    public static final String PACKAGE_NAME = "com.phonepe.app";
    public static final String PHONEPE_VERSION_CODE = "25103107";
    public static final String MANDATE_SETUP_FROM = "Hamburger_Menu";
    public static final String UPI_APP = "PhonePe";
}
