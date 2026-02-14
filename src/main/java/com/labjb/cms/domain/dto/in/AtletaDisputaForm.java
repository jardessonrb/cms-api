package com.labjb.cms.domain.dto.in;

import com.labjb.cms.domain.enums.TipoRegistroPontuacaoEnum;

import java.util.UUID;

public record AtletaDisputaForm(
        UUID atletaId,
        TipoRegistroPontuacaoEnum tipoRegistroDisputa
){}
