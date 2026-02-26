package com.labjb.cms.shared.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    public static Double toMB(Long size){
        return Math.round(((double) size / (1024 * 1024)) * 100.0) / 100.0;
    }

    public static int geraValorEntreZeroAndValorMaximo(int valorMaximo){
        return ThreadLocalRandom.current().nextInt(0, valorMaximo);
    }

    public static double arredondar(Double valor) {
        if (valor == null || valor < 0) return 0.0;

        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
