package com.labjb.cms.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SituacaoDisputaEnum {

    PENDENTE("Pendente"),
    CONCLUIDA("Concluída");

    private String descricao;
}
