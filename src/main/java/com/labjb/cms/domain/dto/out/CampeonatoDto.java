package com.labjb.cms.domain.dto.out;

import java.time.LocalDateTime;
import java.util.UUID;

public record CampeonatoDto(UUID id, String nome, LocalDateTime criadoEm) {
}
