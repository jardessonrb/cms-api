package com.labjb.cms.domain.dto.out;

import com.labjb.cms.domain.enums.TipoDisputaEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DisputaDto(
        UUID id,
        String situacao,
        UUID rodadaId,
        TipoDisputaEnum tipoDisputa,
        List<RegistroDisputaDto> registrosDisputa,
        LocalDateTime criadoEm
) {
}
