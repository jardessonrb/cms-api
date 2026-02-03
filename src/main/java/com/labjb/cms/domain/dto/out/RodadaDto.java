package com.labjb.cms.domain.dto.out;

import com.labjb.cms.domain.enums.SituacaoRodadaEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record RodadaDto(
        UUID id,
        String nome,
        SituacaoRodadaEnum situacao,
        LocalDateTime criadoEm
) {
}
