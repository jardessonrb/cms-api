package com.labjb.cms.shared.errors.handlerexception;

import com.labjb.cms.domain.dto.out.ExceptionDefaultDto;
import com.labjb.cms.shared.errors.handlerexception.handlers.ExceptionHandlerStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RestControllerAdvice
public class ExceptionHandlerAdvice {

    private final List<ExceptionHandlerStrategy> exceptionHandlerStrategies;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDefaultDto> handle(Exception exception, HttpServletRequest request){
        return exceptionHandlerStrategies.stream()
                .filter(handler -> handler.isExceptionSuportada(exception))
                .findFirst()
                .map(handler -> handler.processarException(exception, request))
                .orElseGet(() -> {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ExceptionDefaultDto
                                    .builder()
                                    .erros(Arrays.asList(exception.getMessage()))
                                    .path(request.getRequestURI())
                                    .mensagem(exception.getMessage())
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                                    .build()
                            );
                });
    }
}
