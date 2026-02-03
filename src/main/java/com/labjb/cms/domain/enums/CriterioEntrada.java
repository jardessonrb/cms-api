package com.labjb.cms.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CriterioEntrada {

    TODOS("Todos"),
    N_PRIMEIROS("N primeiros");

    private String descricao;
}
