package com.springboot.financial.exception.impl;

import com.springboot.financial.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class ScrapFailedException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "배당금 정보 스크랩이 실패하였습니다.";
    }
}
