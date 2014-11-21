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

    private int[] levelNodeAmount;
    private int[] nodeOutputCounts;

    private AbstractHTMNode[][] levels;
    private TopPatternClassifier classifier;

    public HTMDNetwork(int levelCount, int xInput, int yInput, int[] levelNodeAmount, int[] levelTimes, int[] nodeOutputCounts) {
        this.levelNodeAmount = levelNodeAmount;
        this.nodeOutputCounts = nodeOutputCounts;
        levels = new AbstractHTMNode[levelCount][];
        for (int i = 0; i < levelCount; i++) {
            levels[i] = new AbstractHTMNode[levelNodeAmount[i]];
            for (int j = 0; j < levelNodeAmount[i]; j++) {
                if (i == 0) {
                    levels[i][j] = new ClusteredHTMNode(30, 4 * xInput * yInput / (levelNodeAmount[i] * levelTimes[i]), nodeOutputCounts[0]);
                } else {
                    levels[i][j] = new SimpleHTMNode(nodeOutputCounts[i]);
                }
            }
        }

        classifier = new TopPatternClassifier(levelNodeAmount[1] * nodeOutputCounts[1]);
    }

    public void learnLevel(int level, double[] image) {
        int nodeAmount = levelNodeAmount[level];
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
        int nodeAmount = levelNodeAmount[level];
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
        for (int i = 0; i < levelNodeAmount[level]; i++) {
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
