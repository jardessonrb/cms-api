package com.labjb.cms.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SituacaoInscricaoCategoriaEnum {
    ATIVO("Ativo"),
    ENCERRADA("Encerrada");

    private String descricao;
}
