package com.labjb.cms.shared.errors.handlerexception.handlers;

import com.labjb.cms.domain.dto.out.ExceptionDefaultDto;
import com.labjb.cms.shared.errors.exception.JwtAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class JwtAuthenticationHandlerException implements ExceptionHandlerStrategy {

    @Override
    public boolean isExceptionSuportada(Exception exception) {
        return exception instanceof JwtAuthenticationException;
    }

    @Override
    public ResponseEntity<ExceptionDefaultDto> processarException(Exception exception, HttpServletRequest httpServletRequest) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ExceptionDefaultDto
                        .builder()
                        .erros(Arrays.asList("Erro de autenticação JWT"))
                        .path(httpServletRequest.getRequestURI())
                        .mensagem(exception.getMessage())
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .build()
                );
    }
}
