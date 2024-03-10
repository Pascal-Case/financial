package com.springboot.financial.exception.impl;

import com.springboot.financial.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class AlreadyExistTickerException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "이미 존재하는 티커 입니다.";
    }
}
