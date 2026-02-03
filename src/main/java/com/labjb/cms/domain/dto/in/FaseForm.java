package com.labjb.cms.domain.dto.in;

import com.labjb.cms.domain.enums.CriterioEntrada;
import java.util.UUID;

public record FaseForm(
        String nome,
        CriterioEntrada criterioEntrada,
        Integer quantidadeAtletas,
        UUID categoriaId
) {}
