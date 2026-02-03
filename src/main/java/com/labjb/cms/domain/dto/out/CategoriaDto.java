package com.labjb.cms.domain.dto.out;

import com.labjb.cms.domain.enums.SituacaoCategoriaEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoriaDto(
        UUID id,
        String nome,
        SituacaoCategoriaEnum situacao,
        LocalDateTime criadoEm
) {
}
