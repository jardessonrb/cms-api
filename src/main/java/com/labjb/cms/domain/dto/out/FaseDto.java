package com.labjb.cms.domain.dto.out;

import com.labjb.cms.domain.enums.CriterioEntrada;
import com.labjb.cms.domain.enums.SituacaoFaseEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record FaseDto(
        UUID id,
        String nome,
        SituacaoFaseEnum situacao,
        CriterioEntrada criterioEntrada,
        Integer quantidadeAtletas,
        Integer quantidadeAtletasInscritos,
        Integer ordem,
        Integer quantidadeRodadas,
        FaseDto faseAnterior,
        Boolean isCompartilhada
) {}
