package com.labjb.cms.domain.dto.out;

public record PontuacaoParcialDto(
        Long atletaId,
        String categoria,
        String fase,
        String competidor,
        Long partidas,
        Long partidasConcluidas,
        Double notaIndividual,
        Double notaDupla,
        Double total,
        Integer posicao
) {}
