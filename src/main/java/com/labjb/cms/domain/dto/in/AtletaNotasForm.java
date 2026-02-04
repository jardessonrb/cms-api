package com.labjb.cms.domain.dto.in;

import java.util.List;
import java.util.UUID;

public record AtletaNotasForm(
        UUID atletaId,
        List<NotaForm> notas
) {
}
