package ru.isa.ai.utils;

import java.util.Arrays;

/**
 * Author: Aleksandr Panov
 * Date: 02.02.2015
 * Time: 20:09
 */
public class StringUtils {
    public static int[] parseIntArray(String str) {
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();
    }
}
