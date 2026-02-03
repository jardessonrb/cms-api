package com.labjb.cms.shared.errors.handlerexception.handlers;

import com.labjb.cms.domain.dto.out.ExceptionDefaultDto;
import com.labjb.cms.shared.errors.exception.EntityAlreadyException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class EntityAlreadyHandlerException implements ExceptionHandlerStrategy {
    @Override
    public boolean isExceptionSuportada(Exception exception) {
        return exception instanceof EntityAlreadyException;
    }

    @Override
    public ResponseEntity<ExceptionDefaultDto> processarException(Exception exception, HttpServletRequest httpServletRequest) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ExceptionDefaultDto
                        .builder()
                        .erros(Arrays.asList(exception.getMessage()))
                        .path(httpServletRequest.getRequestURI())
                        .mensagem(exception.getMessage())
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build()
                );
    }
}
