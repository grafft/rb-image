package ru.isa.ai.clusterers;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Classical 1-D RSOM with error rate neighbourhood calculation
 * and Luttrell interpolating growing
 * <p>
 * Author: Aleksandr Panov
 * Date: 02.02.2015
 * Time: 15:48
 */
public class RSOMClusterer {
    protected double sigma = 4.0;
    protected double gamma = 0.1;
    protected double[][] neurons; // weights of neuron synapses connected to inputs
    protected double[][] diffMemory;

    private int startSize = 2;
    private int inputCounter = 0;
    private int growthCounter = 0;
    private int[] growthRate;

    protected double alpha = 0.1;

    public RSOMClusterer(int inputDimension, int[] growthRate, double alpha) {
        this.alpha = alpha;
        this.growthRate = growthRate;
        neurons = new double[startSize][inputDimension];
        diffMemory = new double[startSize][inputDimension];
        for (int i = 0; i < 0; i++) {
            for (int j = 0; j < inputDimension; j++) {
                neurons[i][j] = Math.random();
                diffMemory[i][j] = 0;
            }
        }
    }

    public double[] process(double[] input) {
        double minDiff = Double.MAX_VALUE; // it is square of min euclidean distance
        int bmu = -1;
        for (int i = 0; i < neurons.length; i++) {
            final int index = i;
            diffMemory[index] = IntStream.range(0, neurons[index].length)
                    .mapToDouble(item -> ((1 - alpha) * diffMemory[index][item] + alpha * (neurons[index][item] - input[item])))
                    .toArray();
            double diffNorm = DoubleStream.of(diffMemory[index])
                    .reduce(0, (res, i2) -> res + i2 * i2);
            if (minDiff > diffNorm) {
                bmu = i;
                minDiff = diffNorm;
            }
        }

        double mu = minDiff / input.length;
        for (int i = 0; i < neurons.length; i++) {
            double h = Math.exp(-(i - bmu) * (i - bmu) / mu * sigma);
            for (int j = 0; j < neurons[i].length; j++)
                neurons[i][j] = neurons[i][j] + gamma * h * diffMemory[i][j];
        }

        double[] result = calculateOutput(input, bmu);
        growing();

        return result;
    }

    private void growing() {
        inputCounter++;
        if (growthRate.length > 0 && inputCounter == growthRate[growthCounter]) {
            double[][] newNeurons = new double[neurons.length * 2 - 1][];
            for (int i = 0; i < neurons.length; i++) {
                if (i % 2 == 0) {
                    newNeurons[2 * i] = neurons[i];
                } else {
                    final int index = i;
                    newNeurons[2 * i + 1] = IntStream.range(0, neurons[i].length)
                            .mapToDouble(item -> (neurons[index - 1][item] + neurons[index][item]) / 2)
                            .toArray();
                }
            }
            neurons = newNeurons;
            growthCounter++;
        }
    }

    protected double[] calculateOutput(double[] input, int bmu) {
        double[] result = new double[neurons.length];
        for (int i = 0; i < neurons.length; i++) {
            result[i] = i == bmu ? 1 : 0;
        }

        return result;
    }
}
