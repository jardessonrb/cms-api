package com.labjb.cms.domain.dto.in;

import java.util.UUID;

public record NotaForm(
        Integer notaDoAtleta,
        Integer notaDaDupla,
        UUID juradoId
) {
}
