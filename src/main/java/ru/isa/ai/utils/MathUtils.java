package ru.isa.ai.utils;

/**
 * Author: Aleksandr Panov
 * Date: 11.02.2015
 * Time: 19:12
 */
public class MathUtils {
    public static double[] flatten(double[][] data) {
        double[] result = new double[data.length * data[0].length];
        for (int i = 0; i < data.length; i++)
            System.arraycopy(data[i], 0, result, i * data[0].length, data[0].length);
        return result;
    }
}
