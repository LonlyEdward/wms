package com.wms.backend.exception;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

public class CreditLimitExceededException extends AppException {

    public CreditLimitExceededException(BigDecimal orderAmount,
                                        BigDecimal outstanding,
                                        BigDecimal creditLimit) {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "CREDIT_LIMIT_EXCEEDED",
                String.format(
                        "Order amount of %s would bring total outstanding to %s, "
                                + "exceeding the credit limit of %s",
                        orderAmount, outstanding.add(orderAmount), creditLimit
                )
        );
    }
}