package com.labjb.cms.domain.dto.out;

import com.labjb.cms.domain.enums.SituacaoCampeonatoEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record CampeonatoDetalhadoDto(
    UUID id, 
    String nome, 
    SituacaoCampeonatoEnum situacao, 
    LocalDateTime criadoEm,
    String nomeUsuarioCriador,
    long quantidadeAtletas,
    long quantidadeCategorias,
    long quantidadeJurados
) {
}
