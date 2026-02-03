package com.labjb.cms.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SituacaoRodadaEnum {

    INICIADA("Iniciada"),
    FINALIZADA("Finalizada");

    private String descricao;
}
