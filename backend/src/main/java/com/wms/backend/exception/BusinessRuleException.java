package com.wms.backend.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends AppException {

    public BusinessRuleException(String errorCode, String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, errorCode, message);
    }
}