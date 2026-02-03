package com.labjb.cms.domain.dto.out;

import lombok.Builder;

import java.util.List;

@Builder
public record ExceptionDefaultDto(
        String mensagem,
        Integer statusCode,
        String path,
        List<String> erros
) {}
