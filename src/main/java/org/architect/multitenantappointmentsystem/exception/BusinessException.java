package org.architect.multitenantappointmentsystem.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends ApiException {
    public BusinessException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT ;
    }

    @Override
    public String getCode() {
        return "BUSINESS_ERROR";
    }

}
