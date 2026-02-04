package com.labjb.cms.domain.dto.in;

import java.util.UUID;

public record JuradoForm(
        String nome,
        String apelido,
        String grupo,
        UUID campeonatoId
) {
}
