package com.labjb.cms.shared.errors.handlerexception.handlers;

import com.labjb.cms.domain.dto.out.ExceptionDefaultDto;
import com.labjb.cms.shared.errors.exception.JwtTokenMissingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class JwtTokenMissingHandlerException implements ExceptionHandlerStrategy {

    @Override
    public boolean isExceptionSuportada(Exception exception) {
        return exception instanceof JwtTokenMissingException;
    }

    @Override
    public ResponseEntity<ExceptionDefaultDto> processarException(Exception exception, HttpServletRequest httpServletRequest) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ExceptionDefaultDto
                        .builder()
                        .erros(Arrays.asList("Token JWT não encontrado"))
                        .path(httpServletRequest.getRequestURI())
                        .mensagem(exception.getMessage())
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .build()
                );
    }
}
