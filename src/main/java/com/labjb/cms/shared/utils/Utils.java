package com.labjb.cms.shared.utils;

import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    public static Double toMB(Long size){
        return Math.round(((double) size / (1024 * 1024)) * 100.0) / 100.0;
    }

    public static int geraValorEntreZeroAndValorMaximo(int valorMaximo){
        return ThreadLocalRandom.current().nextInt(0, valorMaximo + 1);
    }
}
