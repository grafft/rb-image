package ru.isa.ai.htm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.isa.ai.classifiers.TopPatternClassifier;
import ru.isa.ai.utils.MovieUtils;

/**
 * Author: Aleksandr Panov
 * Date: 19.11.2014
 * Time: 19:00
 */
public class HTMDNetwork {
    private static final Logger logger = LogManager.getLogger(HTMDNetwork.class.getSimpleName());

    private int[] xLevelSizes;
    private int[] yLevelSizes;
    private int[] nodeOutputCounts;

    private AbstractHTMNode[][] levels;
    private TopPatternClassifier classifier;

    public HTMDNetwork(int levelCount, int xInput, int yInput, int[] xLevelSizes, int[] yLevelSizes, int[] nodeOutputCounts) {
        this.xLevelSizes = xLevelSizes;
        this.yLevelSizes = yLevelSizes;
        this.nodeOutputCounts = nodeOutputCounts;
        levels = new AbstractHTMNode[levelCount][];
        for (int i = 0; i < levelCount; i++) {
            int levelAmount = xLevelSizes[levelCount] * yLevelSizes[levelCount];
            levels[i] = new AbstractHTMNode[levelAmount];
            for (int j = 0; j < levelAmount; j++) {
                if (i == 0) {
                    levels[i][j] = new ClusteredHTMNode(30, xInput * yInput / (this.xLevelSizes[i] * this.yLevelSizes[i]), nodeOutputCounts[0]);
                } else {
                    levels[i][j] = new SimpleHTMNode(nodeOutputCounts[i]);
                }
            }
        }

        classifier = new TopPatternClassifier(xLevelSizes[1] * yLevelSizes[1] * nodeOutputCounts[1]);
    }

    public void learnLevel(int level, double[] image) {
        int nodeAmount = xLevelSizes[level] * yLevelSizes[level];
        double[] processedInput = image;
        for (int i = 0; i < level; i++) {
            processedInput = processLevel(i, processedInput);
        }

        for (int i = 0; i < nodeAmount / 2; i++) {
            double[][] movie = MovieUtils.createHMovieForNode(image, nodeAmount / 2, i);
            for (double[] aMovie : movie)
                levels[level][i].learn(aMovie);
        }

        for (int i = nodeAmount / 2; i < nodeAmount; i++) {
            double[][] movie = MovieUtils.createVMovieForNode(image, nodeAmount / 2, i);
            for (double[] aMovie : movie)
                levels[level][i].learn(aMovie);
        }
    }

    public double[] processLevel(int level, double[] image) {
        int nodeAmount = xLevelSizes[level] * yLevelSizes[level];
        double[] output = new double[nodeOutputCounts[level] * nodeAmount];

        for (int i = 0; i < nodeAmount / 2; i++) {
            double[][] movie = MovieUtils.createHMovieForNode(image, nodeAmount / 2, i);
            double[] result = levels[level][i].dynamicProcess(movie);
            System.arraycopy(result, 0, output, i * result.length, result.length);
        }

        for (int i = nodeAmount / 2; i < nodeAmount; i++) {
            double[][] movie = MovieUtils.createVMovieForNode(image, nodeAmount / 2, i);
            double[] result = levels[level][i].dynamicProcess(movie);
            System.arraycopy(result, 0, output, i * result.length, result.length);
        }

        return output;
    }

    public void finalizeLevelLearning(int level) {
        for (int i = 0; i < xLevelSizes[level] * xLevelSizes[level]; i++) {
            levels[level][i].finalizeLearning();
        }
    }

    public void learningAll(double[] image, byte label) {
        double[] processedInput = image;
        for (int i = 0; i < levels.length; i++) {
            processedInput = processLevel(i, processedInput);
        }

        classifier.addExample(processedInput, label);
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
