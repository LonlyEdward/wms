package com.wms.backend.exception;

import org.springframework.http.HttpStatus;

public class AccountOnHoldException extends AppException {

    public AccountOnHoldException(String customerName) {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "ACCOUNT_ON_HOLD",
                "Account for '" + customerName
                        + "' is currently on hold. "
                        + "Please contact the admin to release it."
        );
    }
}