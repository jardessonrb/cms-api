package com.labjb.cms.domain.dto.out;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record NotaDto(
        UUID id,
        Integer notaDoAtleta,
        Integer notaDaDupla,
        UUID juradoId,
        String juradoNome
) {}
