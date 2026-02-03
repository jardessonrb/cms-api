package com.labjb.cms.domain.dto.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AtletaDto(
        UUID id,
        String nome,
        String apelido,
        String responsavel,
        LocalDate dataNascimento,
        String cidade,
        String grupo,
        String graduacao,
        LocalDateTime criadoEm
) {
}
