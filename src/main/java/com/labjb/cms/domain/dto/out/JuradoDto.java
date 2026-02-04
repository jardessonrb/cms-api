package com.labjb.cms.domain.dto.out;

import java.time.LocalDateTime;
import java.util.UUID;

public record JuradoDto(
        UUID id,
        String nome,
        String apelido,
        String grupo,
        LocalDateTime criadoEm
) {
}
