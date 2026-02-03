package com.labjb.cms.shared.errors.handlerexception.handlers;

import com.labjb.cms.domain.dto.out.ExceptionDefaultDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class MethodArgumentNotValidHandlerException implements ExceptionHandlerStrategy{

    private final MessageSource messsageSource;

    @Override
    public boolean isExceptionSuportada(Exception exception) {
        return exception instanceof MethodArgumentNotValidException;
    }

    @Override
    public ResponseEntity<ExceptionDefaultDto> processarException(Exception exception, HttpServletRequest httpServletRequest) {
        MethodArgumentNotValidException methodArgumentNotValidException = (MethodArgumentNotValidException)exception;

        List<String> errosDto = new ArrayList<>();
        List<FieldError> camposComErros = methodArgumentNotValidException.getBindingResult().getFieldErrors();

        camposComErros.forEach(erro -> {
            String mensagemErroValidacao = messsageSource.getMessage(erro, LocaleContextHolder.getLocale());
            String erroCampo = "Campo: "+erro.getField()+"; Mensagem: "+mensagemErroValidacao;

            errosDto.add(erroCampo);
        });

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ExceptionDefaultDto
                    .builder()
                    .mensagem("Campos inválidos")
                    .erros(errosDto)
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .path(httpServletRequest.getRequestURI())
                    .build()
                );
    }
}
