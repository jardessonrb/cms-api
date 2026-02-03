package com.labjb.cms.shared.utils;

import org.mapstruct.Named;

import java.util.Optional;

public final class StringUtils {

    @Named("removePontuacao")
    public static String removePontuacao(String texto){
        return Optional.ofNullable(texto)
                .map( textoOpt -> textoOpt.replaceAll("\\D", ""))
                .orElse(texto);
    }
}
