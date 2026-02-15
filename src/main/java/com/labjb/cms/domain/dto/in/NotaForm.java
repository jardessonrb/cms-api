package com.labjb.cms.domain.dto.in;

import java.util.UUID;

public record NotaForm(
        UUID notaId,
        Integer notaDoAtleta,
        Integer notaDaDupla,
        UUID juradoId
) {
}
