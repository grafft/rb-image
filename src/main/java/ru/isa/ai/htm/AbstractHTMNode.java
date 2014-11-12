package ru.isa.ai.htm;

import ru.isa.ai.clusterers.MarkovNetClusterer;
import ru.isa.ai.clusterers.MarkovNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Aleksandr Panov
 * Date: 11.11.2014
 * Time: 17:11
 */
public abstract class AbstractHTMNode {
    public static final double SIGMA = 0.1;

    protected int maxTGNumber = 10;

    protected List<MarkovNode> markovNet = new ArrayList<>();
    protected MarkovNode previous = null;
    protected MarkovNetClusterer clusterer;

    public void learn(double[] input) {
        double[] processed = preProcessInput(input);
        MarkovNode current = getCorrespondingNode(processed);

        if (previous != null) {
            if (!previous.getConnectedNode().containsKey(current))
                previous.getConnectedNode().put(current, 1.0);
            else
                previous.getConnectedNode().put(current, previous.getConnectedNode().get(current) + 1);
        }
        previous = current;
    }

    public void finalizeLearning() {
        normalizeMarkovNet();

        clusterer = new MarkovNetClusterer(markovNet, maxTGNumber);
        clusterer.buildClusters();
    }

    protected void normalizeMarkovNet() {
        for (MarkovNode node : markovNet) {
            double sumTrans = node.getConnectedNode().values().stream().reduce((result, item) -> result + item).get();
            for (MarkovNode transNode : node.getConnectedNode().keySet()) {
                node.getConnectedNode().put(transNode, node.getConnectedNode().get(transNode) / sumTrans);
            }
        }
    }

    public double[] process(double[] input) {
        double[] result = new double[maxTGNumber];
        Map<Integer, Double> clusterDists = new HashMap<>();
        for (int i = 0; i < maxTGNumber; i++)
            clusterDists.put(i, Double.MAX_VALUE);

        for (MarkovNode node : markovNet) {
            double distance = getNodeDistance(node, input);
            if (distance < clusterDists.get(clusterer.getClusterNumbers()[node.getIndex()]))
                clusterDists.put(clusterer.getClusterNumbers()[node.getIndex()], distance);
        }

        normalizeClusterDistances(clusterDists);

        double sum = clusterDists.values().stream().reduce(0.0, Double::sum);
        for (int i = 0; i < maxTGNumber; i++) {
            result[i] = clusterDists.get(i) / sum;
        }
        return result;
    }

    protected abstract double[] preProcessInput(double[] input);

    protected abstract void normalizeClusterDistances(Map<Integer, Double> clusterDists);

    protected abstract MarkovNode getCorrespondingNode(double[] pattern);

    protected abstract double getNodeDistance(MarkovNode node, double[] pattern);

    public void setMaxTGNumber(int maxTGNumber) {
        this.maxTGNumber = maxTGNumber;
    }
}
