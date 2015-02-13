package ru.isa.ai.clusterers.spatial;

import ru.isa.ai.clusterers.RSOMClusterer;

import java.util.stream.IntStream;

/**
 * Author: Aleksandr Panov
 * Date: 02.02.2015
 * Time: 15:48
 */
public class SOMSpatialClusterer extends RSOMClusterer {

    public SOMSpatialClusterer(int inputDimension, int outputDimension, int startSize, int[] growthRate) {
        super(inputDimension, outputDimension, startSize, growthRate);
        alpha = 1.0;
    }

    protected double[] calculateOutput(double[] input) {
        double[] result = new double[neurons.length];
        double bestDist = IntStream.range(0, neurons[currentBMU].length)
                .mapToDouble(item -> (neurons[currentBMU][item] - input[item]))
                .reduce(0, (res, item) -> res + item * item);

        for (int i = 0; i < neurons.length; i++) {
            if (neurons[i] != null) {
                final int index = i;
                double dist = IntStream.range(0, neurons[index].length)
                        .mapToDouble(item -> (neurons[index][item] - input[item]))
                        .reduce(0, (res, item) -> res + item * item);
                result[i] = bestDist / dist;
            }
        }

        return result;
    }
}
