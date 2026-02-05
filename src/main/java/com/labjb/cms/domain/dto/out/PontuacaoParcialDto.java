package com.labjb.cms.domain.dto.out;

import java.math.BigDecimal;

public record PontuacaoParcialDto(
        Long atletaId,
        String categoria,
        String fase,
        String competidor,
        Long partidas,
        Long partidasConcluidas,
        BigDecimal notaIndividual,
        BigDecimal notaDupla,
        BigDecimal total,
        Integer posicao
) {}
