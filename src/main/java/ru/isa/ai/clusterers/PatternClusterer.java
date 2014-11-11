package ru.isa.ai.clusterers;

import java.util.stream.IntStream;

/**
 * Online K-Means Clusterer
 * Author: Aleksandr Panov
 * Date: 31.10.2014
 * Time: 17:40
 */
public class PatternClusterer {
    private int k;
    private double[][] means;
    private int[] counts;

    public PatternClusterer(int k, int dimension) {
        this.k = k;
        means = new double[k][dimension];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < dimension; j++) {
                means[i][j] = Math.random();
            }
        }
        counts = new int[k];
    }

    public int updateClusterer(double[] pattern) {
        double distance = Double.MAX_VALUE;
        int closest = -1;
        for (int i = 0; i < k; i++) {
            double dist = getDistance(means[i], pattern);
            if (dist < distance) {
                distance = dist;
                closest = i;
            }
        }
        counts[closest]++;
        for (int i = 0; i < means[closest].length; i++)
            means[closest][i] = means[closest][i] + (pattern[i] - means[closest][i]) / counts[closest];
        return closest;
    }

    private double getDistance(double[] mean, double[] pattern) {
        double distance = IntStream.range(0, mean.length)
                .mapToDouble(item -> (mean[item] - pattern[item]))
                .reduce(0, (res, i2) -> res + i2 * i2);
        return Math.sqrt(distance);
    }

    public double[][] getMeans() {
        return means;
    }
}
