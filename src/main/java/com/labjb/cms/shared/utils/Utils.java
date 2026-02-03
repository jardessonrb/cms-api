package com.labjb.cms.shared.utils;

public class Utils {

    public static Double toMB(Long size){
        return Math.round(((double) size / (1024 * 1024)) * 100.0) / 100.0;
    }
}
