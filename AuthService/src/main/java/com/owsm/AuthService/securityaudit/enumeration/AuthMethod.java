package com.owsm.AuthService.securityaudit.enumeration;

/**
 * Common authentication method values. Audit records persist the method as a
 * string so new methods can be recorded without a database enum migration.
 */
public enum AuthMethod {
    PASSWORD,
    MFA,
    PASSWORD_AND_MFA,
    OTP,
    EMAIL_OTP,
    SMS_OTP,
    TOTP,
    PASSKEY,
    BIOMETRIC,
    OAUTH2,
    OPENID_CONNECT,
    SSO,
    API_KEY,
    REFRESH_TOKEN,
    CAMDIGIKEY,
    MAGIC_LINK,
    UNKNOWN
}
