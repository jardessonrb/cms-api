package com.labjb.cms.domain.dto.out;

import java.util.UUID;

public record RegistroDisputaDto(
        UUID id,
        String nomeAtleta,
        String apelidoAtleta,
        UUID atletaId
) {
}
