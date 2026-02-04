package com.labjb.cms.domain.dto.out;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotaDto(
        UUID id,
        Integer notaDoAtleta,
        Integer notaDaDupla,
        UUID juradoId,
        String juradoNome
) {}
