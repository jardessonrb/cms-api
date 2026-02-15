package com.labjb.cms.domain.dto.out;

import com.labjb.cms.domain.enums.TipoRegistroPontuacaoEnum;

import java.util.List;
import java.util.UUID;

public record RegistroDisputaDto(
        UUID id,
        String nomeAtleta,
        Integer numeroAtleta,
        String apelidoAtleta,
        UUID atletaId,
        TipoRegistroPontuacaoEnum tipoRegistro,
        List<NotaDto> notas
) {}
