package com.labjb.cms.domain.dto.out;

import java.util.List;

public record ValidacaoCorteDto(
        Integer quantidadeEmpatados,
        List<ResultadoFaseAtletaDto> atletasEmpatados
) {}
