package ru.isa.ai.htm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.isa.ai.classifiers.TopPatternClassifier;

/**
 * Author: Aleksandr Panov
 * Date: 23.10.2014
 * Time: 11:13
 */
public class HTMNetwork {
    private static final Logger logger = LogManager.getLogger(HTMNetwork.class.getSimpleName());

    private int[] xLevelSizes = new int[]{7, 4};
    private int[] yLevelSizes = new int[]{7, 4};

    private int[] nodeOutputCounts = new int[]{9, 4};

    private int[] inputCounts = new int[]{28 * 28, xLevelSizes[0] * yLevelSizes[0] * nodeOutputCounts[0],
            xLevelSizes[1] * yLevelSizes[1] * nodeOutputCounts[1]};

    private AbstractHTMNode[][] levels = new AbstractHTMNode[][]{
            new AbstractHTMNode[xLevelSizes[0] * yLevelSizes[0]],
            new AbstractHTMNode[xLevelSizes[1] * yLevelSizes[1]]
    };

    private TopPatternClassifier classifier = new TopPatternClassifier(xLevelSizes[1] * yLevelSizes[1] * nodeOutputCounts[1]);

    public HTMNetwork() {
        logger.info(String.format("Initialization: %d levels", levels.length));
        for (int i = 0; i < levels.length; i++) {
            for (int j = 0; j < levels[i].length; j++) {
                levels[i][j] = i == 0 ? new ClusteredHTMNode(30, inputCounts[i] / (xLevelSizes[0] * yLevelSizes[0]), nodeOutputCounts[0]) :
                        new SimpleHTMNode(nodeOutputCounts[i]);
            }
        }
    }

    public HTMNetwork(int levelCount, int xInput, int yInput, int[] xLevelSizes, int[] yLevelSizes,
                      int[] nodeOutputCounts) {
        this.xLevelSizes = xLevelSizes;
        this.yLevelSizes = yLevelSizes;
        this.nodeOutputCounts = nodeOutputCounts;
        inputCounts[0] = xInput * yInput;
        levels = new AbstractHTMNode[levelCount][];
        for (int i = 0; i < levelCount; i++) {
            int levelAmount = xLevelSizes[levelCount] * yLevelSizes[levelCount];
            levels[i] = new AbstractHTMNode[levelAmount];
            inputCounts[i + 1] = xLevelSizes[i] * yLevelSizes[i] * nodeOutputCounts[i];
            for (int j = 0; j < levelAmount; j++) {
                if (i == 0) {
                    levels[i][j] = new ClusteredHTMNode(30, inputCounts[i] / (this.xLevelSizes[i] * this.yLevelSizes[i]), nodeOutputCounts[0]);
                } else {
                    levels[i][j] = new SimpleHTMNode(nodeOutputCounts[i]);
                }
            }
        }
    }

    public void learnLevel(int level, double[][] movie) {
        for (double[] aMovie : movie) {
            double[] processedInput = aMovie;
            for (int i = 0; i < level; i++) {
                processedInput = processLevel(i, processedInput);
            }
            for (int j = 0; j < xLevelSizes[level] * yLevelSizes[level]; j++) {
                double[] input = cutInputForLevel(level, j, processedInput);
                levels[level][j].learn(input);
            }
        }
    }

    public void finalizeLevelLearning(int level) {
        for (int i = 0; i < xLevelSizes[level] * xLevelSizes[level]; i++) {
            levels[level][i].finalizeLearning();
        }
    }

    public double[] cutInputForLevel(int level, int index, double[] input) {
        int side = (int) Math.round(Math.sqrt(inputCounts[level]));
        int xSize = side / xLevelSizes[level];
        int ySize = side / yLevelSizes[level];

        int startY = index / xLevelSizes[level];
        int startX = index % xLevelSizes[level];

        double[] cutInput = new double[xSize * ySize];
        for (int i = 0; i < ySize; i++) {
            for (int j = 0; j < xSize; j++) {
                cutInput[i * xSize + j] = input[(startY * ySize + i) * side + startX * xSize + j];
            }
        }
        return cutInput;
    }

    public double[] processLevel(int level, double[] input) {
        double[] output = new double[inputCounts[level + 1]];
        for (int i = 0; i < xLevelSizes[level] * yLevelSizes[level]; i++) {
            double[] cutInput = cutInputForLevel(level, i, input);
            double[] result = levels[level][i].process(cutInput);
            System.arraycopy(result, 0, output, i * result.length, result.length);
        }
        return output;
    }

    public void learningAll(double[][] movie, byte label) {
        for (double[] aMovie : movie) {
            double[] processedInput = aMovie;
            for (int i = 0; i < levels.length; i++) {
                processedInput = processLevel(i, processedInput);
            }

            classifier.addExample(processedInput, label);
        }
    }

    public void prepare() {
        classifier.buildModel();
    }

    public byte recognize(double[] image) {
        double[] processedInput = image;
        for (int i = 0; i < levels.length; i++) {
            processedInput = processLevel(i, processedInput);
        }
        return classifier.classify(processedInput);
    }
}
