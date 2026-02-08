package com.labjb.cms.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SituacaoFaseEnum {

    CRIADA("Criada"),
    INICIADA("Iniciada"),
    FINALIZADA("Finalizada"),
    AGUARDANDO_DESEMPATE("Aguardando Desempate");

    private String descricao;
}
