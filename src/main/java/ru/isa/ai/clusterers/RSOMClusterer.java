package ru.isa.ai.clusterers;

import java.util.Arrays;
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

    private int outputDimension;
    private int startSize = 2;
    private int inputCounter = 0;
    private int growthCounter = 0;
    private int[] growthRate;

    protected double alpha = 0.1;

    public RSOMClusterer(int inputDimension, int outputDimension, int[] growthRate) {
        this.outputDimension = outputDimension;
        this.growthRate = growthRate;
        neurons = new double[outputDimension][];
        diffMemory = new double[outputDimension][];
        for (int i = 0; i < outputDimension; i++) {
            if (i % ((outputDimension - 1) / (startSize - 1)) == 0) {
                neurons[i] = new double[inputDimension];
                diffMemory[i] = new double[inputDimension];
                for (int j = 0; j < inputDimension; j++) {
                    neurons[i][j] = Math.random();
                }
            } else {
                neurons[i] = null;
                diffMemory[i] = new double[inputDimension];
            }

        }
    }

    public double[] process(double[] input) {
        double minDiff = Double.MAX_VALUE; // it is square of min euclidean distance
        int bmu = -1;
        for (int i = 0; i < neurons.length; i++) {
            if (neurons[i] != null) {
                final int index = i;
                diffMemory[index] = IntStream.range(0, neurons[index].length)
                        .mapToDouble(item -> ((1 - alpha) * diffMemory[index][item] + alpha * (input[item] - neurons[index][item])))
                        .toArray();
                double diffNorm = DoubleStream.of(diffMemory[index])
                        .reduce(0, (res, i2) -> res + i2 * i2);
                if (minDiff > diffNorm) {
                    bmu = i;
                    minDiff = diffNorm;
                }
            }
        }

        double mu = minDiff / input.length;
        for (int i = 0; i < neurons.length; i++) {
            if (neurons[i] != null) {
                double h = Math.exp(-(i - bmu) * (i - bmu) / mu * sigma);
                for (int j = 0; j < neurons[i].length; j++)
                    neurons[i][j] = neurons[i][j] + gamma * h * diffMemory[i][j];
            }
        }

        double[] result = calculateOutput(input, bmu);
        growing();

        return result;
    }

    private void growing() {
        inputCounter++;
        int prevIndex = -1;
        if (growthRate.length > 0 && growthCounter < growthRate.length && inputCounter == growthRate[growthCounter]) {
            for (int i = 0; i < neurons.length; i++) {
                if (neurons[i] != null) {
                    if (prevIndex != -1) {
                        final int i1 = prevIndex, i2 = i;
                        neurons[(i + prevIndex) / 2] = IntStream.range(0, neurons[i2].length)
                                .mapToDouble(item -> (neurons[i1][item] + neurons[i2][item]) / 2)
                                .toArray();
                    }
                    prevIndex = i;
                }
            }
            growthCounter++;
        }
    }

    protected double[] calculateOutput(double[] input, int bmu) {
        double[] result = new double[outputDimension];
        for (int i = 0; i < neurons.length; i++) {
            result[i] = i == bmu ? 1 : 0;
        }

        return result;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < neurons.length; i++) {
            if(neurons[i] != null){
                builder.append(Arrays.toString(neurons[i]));
            }else{
                builder.append("NULL");
            }

            if(i < neurons.length - 1)
                builder.append("\n");
        }
        return builder.toString();
    }

    public void setSigma(double sigma) {
        this.sigma = sigma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double[][] getNeurons() {
        return neurons;
    }
}
