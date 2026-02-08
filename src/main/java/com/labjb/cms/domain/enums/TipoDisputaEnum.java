package com.labjb.cms.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TipoDisputaEnum {
    
    DUPLA("Dupla"),
    INDIVIDUAL("Individual");

    private String descricao;
}
