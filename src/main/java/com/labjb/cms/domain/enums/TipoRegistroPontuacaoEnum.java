package com.labjb.cms.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TipoRegistroPontuacaoEnum {
    
    PONTUADO("Pontuado"),
    NAO_PONTUADO("Não Pontuado");

    private String descricao;
}
