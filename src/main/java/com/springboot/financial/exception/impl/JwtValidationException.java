package com.springboot.financial.exception.impl;

import com.springboot.financial.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class JwtValidationException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "유효하지 않은 토큰입니다.";
    }
}
