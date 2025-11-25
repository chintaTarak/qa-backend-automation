package org.jarApiAutomation.endpoints;

public class AuthServiceEndpoints {
    // Versioning
    public static final String V1 = "v1";
    public static final String V2 = "v2";
    public static final String V3 = "v3";

    // Authorization
    public static final String REQUEST_OTP = "/api/auth/requestOTP";
    public static final String VERIFY_OTP = "/api/auth/verifyOTP";
    public static final String FETCH_OTP =  "/api/admin/otp/decryptOTP";
    public static final String RESET_OTP = "/api/internal/auth/resetOtp";
    public static final String RESET_VERIFY_OTP_LIMIT = "/api/internal/auth/resetOtpVerifyLimit";
}


// Create Interface for version handler