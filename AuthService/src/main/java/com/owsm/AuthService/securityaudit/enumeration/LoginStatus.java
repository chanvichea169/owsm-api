package com.owsm.AuthService.securityaudit.enumeration;

public enum LoginStatus {
    SUCCESS,
    FAILED,
    LOCKED,
    MFA_REQUIRED,
    MFA_FAILED,
    TOKEN_EXPIRED,
    ACCOUNT_DISABLED,
    ACCOUNT_NOT_FOUND
}
