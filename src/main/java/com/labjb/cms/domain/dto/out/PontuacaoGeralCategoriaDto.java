package com.labjb.cms.domain.dto.out;

public record PontuacaoGeralCategoriaDto(
        Long atletaId,
        String situacao,
        String categoria,
        String competidor,
        String graduacao,
        Integer numeroCompetidor,
        Double pontuacaoPorDupla,
        Double pontuacaoPorAtleta,
        Double totalGeral,
        Integer posicao
) {}
