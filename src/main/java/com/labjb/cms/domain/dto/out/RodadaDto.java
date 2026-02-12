package com.labjb.cms.domain.dto.out;

import com.labjb.cms.domain.enums.SituacaoRodadaEnum;
import com.labjb.cms.domain.enums.TipoRodadaEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record RodadaDto(
        UUID id,
        String nome,
        SituacaoRodadaEnum situacao,
        TipoRodadaEnum tipoRodada,
        Integer atletasParaProximaFase,
        LocalDateTime criadoEm,
        Long disputasConcluidas,
        Long disputasPendentes
) {
}
