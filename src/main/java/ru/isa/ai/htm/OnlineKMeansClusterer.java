package ru.isa.ai.htm;

import java.util.stream.IntStream;

/**
 * Author: Aleksandr Panov
 * Date: 31.10.2014
 * Time: 17:40
 */
public class OnlineKMeansClusterer {
    private int k;
    private double[][] means;
    private int[] counts;

    public OnlineKMeansClusterer(int k, int dimension) {
        this.k = k;
        means = new double[k][dimension];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < dimension; j++) {
                means[i][j] = Math.random();
            }
        }
        counts = new int[k];
    }

    public void updateClusterer(byte[] pattern) {
        double distance = Double.MAX_VALUE;
        int closest = -1;
        for (int i = 0; i < k; i++) {
            double dist = getDistance(means[i], pattern);
            if (dist < distance)
                closest = i;
        }
        counts[closest]++;
        for (int i = 0; i < means[closest].length; i++)
            means[closest][i] = means[closest][i] + (pattern[i] - means[closest][i]) / counts[closest];
    }

    private double getDistance(double[] mean, byte[] pattern) {
        double distance = IntStream.range(0, mean.length)
                .mapToDouble(item -> (mean[item] - pattern[item]) * (mean[item] - pattern[item]))
                .reduce((i1, i2) -> i1 + i2)
                .getAsDouble();
        return Math.sqrt(distance);
    }

    public double[][] getMeans() {
        return means;
    }
}
