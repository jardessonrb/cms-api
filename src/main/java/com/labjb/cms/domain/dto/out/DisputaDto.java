package com.labjb.cms.domain.dto.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DisputaDto(
        UUID id,
        String situacao,
        UUID rodadaId,
        List<RegistroDisputaDto> registrosDisputa,
        LocalDateTime criadoEm
) {
}
