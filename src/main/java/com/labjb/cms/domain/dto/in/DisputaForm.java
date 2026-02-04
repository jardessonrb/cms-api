package com.labjb.cms.domain.dto.in;

import java.util.UUID;

public record DisputaForm(
        UUID atletaAId,
        UUID atletaBId
) {
}
