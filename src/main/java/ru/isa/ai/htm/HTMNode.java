package ru.isa.ai.htm;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Author: Aleksandr Panov
 * Date: 23.10.2014
 * Time: 11:14
 */
public class HTMNode {
    public static final double SIGMA = 0.1;

    private int maxTGNumber = 10;

    private List<MarkovNode> markovNet = new ArrayList<>();
    private MarkovNode previous = null;
    private AgglomerativeHierarchicalClusterer clusterer;

    public void learn(byte[] input) {
        int found = IntStream.range(0, markovNet.size())
                .filter(index -> Arrays.equals(markovNet.get(index).getPattern(), input))
                .findAny().orElse(-1);
        if (found == -1) {
            found = markovNet.size();
            markovNet.add(new MarkovNode(found, input));
        }
        MarkovNode current = markovNet.get(found);
        if (previous != null) {
            if (!previous.getConnectedNode().containsKey(current))
                previous.getConnectedNode().put(current, 1.0);
            else
                previous.getConnectedNode().put(current, previous.getConnectedNode().get(current) + 1);
        }
        previous = current;
    }

    public void generateTemporalGroups() {
        normalize();

        clusterer = new AgglomerativeHierarchicalClusterer(markovNet, maxTGNumber);
        clusterer.buildClusters();
    }


    private void normalize() {
        for (MarkovNode node : markovNet) {
            double sumTrans = node.getConnectedNode().values().stream().reduce((result, item) -> result + item).get();
            for (MarkovNode transNode : node.getConnectedNode().keySet()) {
                node.getConnectedNode().put(transNode, node.getConnectedNode().get(transNode) / sumTrans);
            }
        }
    }

    public double[] process(byte[] input) {
        double[] result = new double[maxTGNumber];
        Map<Integer, Double> clusterDists = new HashMap<>();
        for (int i = 0; i < maxTGNumber; i++)
            clusterDists.put(i, Double.MAX_VALUE);

        for (MarkovNode node : markovNet) {
            double dist = IntStream.range(0, node.getPattern().length)
                    .map(index -> Math.abs(node.getPattern()[index] - input[index]))
                    .sum() / (0.0 + input.length);
            if (dist < clusterDists.get(clusterer.getClusterNumbers()[node.getIndex()]))
                clusterDists.put(clusterer.getClusterNumbers()[node.getIndex()], dist);
        }
        double sum = 0;
        for (int i = 0; i < maxTGNumber; i++) {
            double dist = clusterDists.get(i);
            double newDist = Math.exp(-dist * dist / SIGMA);
            sum += newDist;
            clusterDists.put(i, newDist);
        }

        for (int i = 0; i < maxTGNumber; i++) {
            result[i] = clusterDists.get(i) / sum;
        }
        return result;
    }

    public void setMaxTGNumber(int maxTGNumber) {
        this.maxTGNumber = maxTGNumber;
    }
}
