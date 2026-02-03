package com.labjb.cms.shared.errors.handlerexception.handlers;

import com.labjb.cms.domain.dto.out.ExceptionDefaultDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface ExceptionHandlerStrategy {
    boolean isExceptionSuportada(Exception exception);
    ResponseEntity<ExceptionDefaultDto> processarException(Exception exception, HttpServletRequest httpServletRequest);
}
