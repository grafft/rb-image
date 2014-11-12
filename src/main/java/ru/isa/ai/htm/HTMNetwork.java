package ru.isa.ai.htm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.isa.ai.classifiers.TopPatternClassifier;

import java.util.stream.IntStream;

/**
 * Author: Aleksandr Panov
 * Date: 23.10.2014
 * Time: 11:13
 */
public class HTMNetwork {
    private static final Logger logger = LogManager.getLogger(HTMNetwork.class.getSimpleName());

    private int[] xNodeCounts = new int[]{7, 4};
    private int[] yNodeCounts = new int[]{7, 4};

    private int[] outputCounts = new int[]{28 * 28, xNodeCounts[0] * xNodeCounts[0] * 10, xNodeCounts[1] * xNodeCounts[1] * 5};

    private AbstractHTMNode[][] levels = new AbstractHTMNode[][]{
            new AbstractHTMNode[xNodeCounts[0] * yNodeCounts[0]],
            new AbstractHTMNode[xNodeCounts[1] * yNodeCounts[1]]
    };

    private TopPatternClassifier classifier = new TopPatternClassifier(outputCounts[2]);

    public HTMNetwork() {
        logger.info(String.format("Initialization: %d levels", levels.length));
        for (int i = 0; i < levels.length; i++) {
            for (int j = 0; j < levels[i].length; j++) {
                levels[i][j] = i > 0 ? new SimpleHTMNode() :
                        new ClusteredHTMNode(15, outputCounts[i] / (xNodeCounts[i] * yNodeCounts[i]));
            }
        }
    }

    public void learnLevel(int level, byte[][] movie) {
        logger.debug(String.format("Learn %d level with %d inputs", level, movie.length));
        for (byte[] aMovie : movie) {
            double[] processedInput = IntStream.range(0, aMovie.length)
                    .mapToDouble(index -> (double) (aMovie[index]))
                    .toArray();
            for (int i = 0; i < level; i++) {
                processedInput = processLevel(level, processedInput);
            }
            for (int j = 0; j < xNodeCounts[level] * yNodeCounts[level]; j++) {
                double[] input = cutInputForLevel(level, j, processedInput);
                levels[level][j].learn(input);
            }
        }
    }

    public void finalizeLevelLearning(int level) {
        for (int i = 0; i < xNodeCounts[level] * xNodeCounts[level]; i++) {
            levels[level][i].finalizeLearning();
        }
    }

    public double[] cutInputForLevel(int level, int index, double[] input) {
        int xSize = (int) Math.round(Math.sqrt(outputCounts[level]) / xNodeCounts[level]);
        int ySize = (int) Math.round(Math.sqrt(outputCounts[level]) / yNodeCounts[level]);

        int startY = index / xNodeCounts[level];
        int startX = index % xNodeCounts[level];

        double[] cutInput = new double[xSize * ySize];
        for (int i = 0; i < ySize; i++) {
            for (int j = 0; j < xSize; j++) {
                cutInput[i * xSize + j] = input[(startY + i) * xSize * ySize + startX + j];
            }
        }
        return cutInput;
    }

    public double[] processLevel(int level, double[] input) {
        double[] output = new double[outputCounts[level + 1]];
        for (int i = 0; i < xNodeCounts[level] * yNodeCounts[level]; i++) {
            double[] cutInput = cutInputForLevel(level, i, input);
            double[] result = levels[level][i].process(cutInput);
            System.arraycopy(result, 0, output, i * result.length, result.length);
        }
        return output;
    }

    public void learningAll(byte[][] movie, byte label) {
        logger.debug("Learn top with bayes: " + movie.length);
        // learn top
        for (byte[] aMovie : movie) {
            double[] processedInput = IntStream.range(0, aMovie.length)
                    .mapToDouble(index -> (double) (aMovie[index]))
                    .toArray();
            for (int i = 0; i < levels.length; i++) {
                processedInput = processLevel(i, processedInput);
            }

            classifier.addExample(processedInput, label);
        }
    }

    public void prepare() {
        classifier.buildModel();
    }

    public byte recognize(byte[] image) {
        double[] processedInput = IntStream.range(0, image.length)
                .mapToDouble(index -> (double) (image[index]))
                .toArray();
        for (int i = 0; i < levels.length; i++) {
            processedInput = processLevel(i, processedInput);
        }
        return classifier.classify(processedInput);
    }
}
