package com.labjb.cms.domain.dto.out;

import java.util.UUID;

public record ResultadoFaseAtletaDto(
        Long id,
        UUID atletaUuid,
        String atletaNome,
        Integer atletaNumero,
        Double notaIndividual,
        Double notaDupla,
        Double total,
        Integer posicao
) {}
