package com.labjb.cms.domain.dto.out;

import com.labjb.cms.domain.enums.SituacaoAtletaEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AtletaDto(
        UUID id,
        String nome,
        Integer numero,
        String apelido,
        String responsavel,
        LocalDate dataNascimento,
        String cidade,
        String grupo,
        String graduacao,
        SituacaoAtletaEnum situacao,
        LocalDateTime criadoEm,
        UUID categoriaId,
        String categoria
) {
}
