package com.labjb.cms.domain.dto.in;

import java.time.LocalDate;
import java.util.UUID;

public record AtletaForm(
        String nome,
        Integer numero,
        String apelido,
        String responsavel,
        LocalDate dataNascimento,
        String cidade,
        String grupo,
        String graduacao,
        UUID campeonatoId,
        UUID categoriaId
) {
}
