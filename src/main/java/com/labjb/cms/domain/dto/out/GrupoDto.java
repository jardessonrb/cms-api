package com.labjb.cms.domain.dto.out;

import java.time.LocalDateTime;
import java.util.UUID;

public record GrupoDto(UUID id, String nome, String proprietarioNome, LocalDateTime criadoEm) {
}
