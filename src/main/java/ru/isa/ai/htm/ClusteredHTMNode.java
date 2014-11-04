package ru.isa.ai.htm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Author: Aleksandr Panov
 * Date: 31.10.2014
 * Time: 19:01
 */
public class ClusteredHTMNode {
    public static final double SIGMA = 0.1;

    protected int maxTGNumber = 10;
    private int k = 50;
    private OnlineKMeansClusterer inputClusterer;
    protected List<MarkovNode> clusteredNet = new ArrayList<>();
    protected ClusteredMarkovNode previous = null;
    protected AgglomerativeHierarchicalClusterer clusterer;

    public ClusteredHTMNode(int k, int dimension) {
        this.k = k;
        inputClusterer = new OnlineKMeansClusterer(k, dimension);
        for (int i = 0; i < k; i++) {
            clusteredNet.add(new ClusteredMarkovNode(i, inputClusterer.getMeans()[i]));
        }
    }

    public void learn(byte[] input) {
        int found = inputClusterer.updateClusterer(input);
        ClusteredMarkovNode current = (ClusteredMarkovNode) clusteredNet.get(found);
        if (previous != null) {
            if (!previous.getConnectedNode().containsKey(current))
                previous.getConnectedNode().put(current, 1.0);
            else
                previous.getConnectedNode().put(current, previous.getConnectedNode().get(current) + 1);
        }
        previous = current;
    }

    public void generateTemporalGroups() {
        for (int i = 0; i < k; i++)
            ((ClusteredMarkovNode) clusteredNet.get(i)).setFullPattern(inputClusterer.getMeans()[i]);

        for (MarkovNode node : clusteredNet) {
            if (node.getConnectedNode().size() > 0) {
                double sumTrans = node.getConnectedNode().values().stream().reduce((result, item) -> result + item).get();
                for (MarkovNode transNode : node.getConnectedNode().keySet()) {
                    node.getConnectedNode().put(transNode, node.getConnectedNode().get(transNode) / sumTrans);
                }
            }
        }

        clusterer = new AgglomerativeHierarchicalClusterer(clusteredNet, maxTGNumber);
        clusterer.buildClusters();
    }

    public double[] process(byte[] input) {
        double[] result = new double[maxTGNumber];
        Map<Integer, Double> clusterDists = new HashMap<>();
        for (int i = 0; i < maxTGNumber; i++)
            clusterDists.put(i, Double.MAX_VALUE);

        for (MarkovNode node : clusteredNet) {
            double distance = IntStream.range(0, node.getPattern().length)
                    .mapToDouble(index -> node.getPattern()[index] - input[index])
                    .reduce(0, (res, i) -> res + i * i);
            distance = Math.sqrt(distance);
            if (distance < clusterDists.get(clusterer.getClusterNumbers()[node.getIndex()]))
                clusterDists.put(clusterer.getClusterNumbers()[node.getIndex()], distance);
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
