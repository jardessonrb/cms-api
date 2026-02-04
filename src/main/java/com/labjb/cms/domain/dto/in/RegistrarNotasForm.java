package com.labjb.cms.domain.dto.in;

public record RegistrarNotasForm(
        AtletaNotasForm primeiroAtleta,
        AtletaNotasForm segundoAtleta
) {
}
