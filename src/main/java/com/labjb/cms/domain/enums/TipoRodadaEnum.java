package com.labjb.cms.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoRodadaEnum {

    NORMAL("Normal"),
    DESEMPATE("Desempate");

    private String descricao;
}
