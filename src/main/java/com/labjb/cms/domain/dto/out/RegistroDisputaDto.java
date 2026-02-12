package com.labjb.cms.domain.dto.out;

import com.labjb.cms.domain.enums.TipoRegistroDisputaEnum;

import java.util.UUID;

public record RegistroDisputaDto(
        UUID id,
        String nomeAtleta,
        Integer numeroAtleta,
        String apelidoAtleta,
        UUID atletaId,
        TipoRegistroDisputaEnum tipoRegistro
) {
}
