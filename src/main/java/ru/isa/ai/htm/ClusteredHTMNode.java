package ru.isa.ai.htm;

import ru.isa.ai.clusterers.MarkovNode;
import ru.isa.ai.clusterers.PatternClusterer;

import java.util.Map;
import java.util.stream.IntStream;

/**
 * Author: Aleksandr Panov
 * Date: 31.10.2014
 * Time: 19:01
 */
public class ClusteredHTMNode extends AbstractHTMNode {
    private int patternsAmount;
    private PatternClusterer inputClusterer;

    public ClusteredHTMNode(int k, int dimension, int maxTGNumber) {
        super(maxTGNumber);
        this.patternsAmount = k;
        inputClusterer = new PatternClusterer(k, dimension);
        for (int i = 0; i < k; i++) {
            markovNet.add(new MarkovNode(i, inputClusterer.getMeans()[i]));
        }
    }

    @Override
    protected double[] preProcessInput(double[] input) {
        return input;
    }

    @Override
    protected MarkovNode getCorrespondingNode(double[] pattern) {
        int found = inputClusterer.updateClusterer(pattern);
        return markovNet.get(found);
    }

    @Override
    protected double getNodeDistance(MarkovNode node, double[] pattern) {
        double distance = IntStream.range(0, node.getPattern().length)
                .mapToDouble(index -> node.getPattern()[index] - pattern[index])
                .reduce(0, (res, i) -> res + i * i);
        return Math.sqrt(distance);
    }

    @Override
    protected void normalizeMarkovNet() {
        for (int i = 0; i < patternsAmount; i++)
            markovNet.get(i).setPattern(inputClusterer.getMeans()[i]);

        super.normalizeMarkovNet();
    }

    @Override
    protected double[] normalizeClusterDistances(Map<Integer, Double> clusterDists) {
        // TODO AP: it is part of algorithm but why it is required?
//        for (int i = 0; i < clusterDists.size(); i++) {
//            double dist = clusterDists.get(i);
//            double newDist = Math.exp(-dist * dist / SIGMA);
//            clusterDists.put(i, newDist);
//        }
        return super.normalizeClusterDistances(clusterDists);
    }
}
