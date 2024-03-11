package com.springboot.financial.exception.impl;

import com.springboot.financial.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class InvalidTickerException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "존재하지 않는 티커입니다.";
    }
}
