package com.labjb.cms.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SituacaoCampeonatoEnum {
    CRIADO("Criado"),
    INICIADO("Iniciado"),
    FINALIZADO("Finalizado");

    private String descricao;
}
