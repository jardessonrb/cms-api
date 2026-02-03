package com.labjb.cms.domain.dto.in;

import java.util.UUID;

public record CategoriaForm(
        String nome,
        UUID campeonatoId
) {
}
