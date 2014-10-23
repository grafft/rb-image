package ru.isa.ai.htm;

import weka.clusterers.HierarchicalClusterer;
import weka.core.*;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Author: Aleksandr Panov
 * Date: 23.10.2014
 * Time: 11:14
 */
public class HTMNode {
    public static final int MAX_TG_NUMBER = 10;
    public static final double SIGMA = 0.05;

    private HierarchicalClusterer clusterer;

    private List<MarkovNode> markovNet = new ArrayList<>();
    private MarkovNode previous = null;

    public HTMNode() {
        clusterer = new HierarchicalClusterer();
        clusterer.setNumClusters(MAX_TG_NUMBER);
    }

    public void learn(byte[] input) {
        int found = IntStream.range(0, markovNet.size())
                .filter(index -> Arrays.equals(markovNet.get(index).pattern, input))
                .findAny().orElse(-1);
        if (found == -1) {
            found = markovNet.size();
            markovNet.add(new MarkovNode(found, input));
        }
        MarkovNode current = markovNet.get(found);
        if (previous != null) {
            if (!previous.connectedNode.containsKey(current))
                previous.connectedNode.put(current, 1.0);
            else
                previous.connectedNode.put(current, previous.connectedNode.get(current) + 1);
        }
        previous = current;
    }

    public void generateTemporalGroups() {
        normalize();

        ArrayList<Attribute> attrInfo = new ArrayList<>();
        for (int i = 0; i < markovNet.size(); i++) {
            attrInfo.add(new Attribute("dist_" + i));
        }
        for (int i = 0; i < markovNet.get(0).pattern.length; i++) {
            attrInfo.add(new Attribute("pat_" + i));
        }
        Instances data = new Instances("data", attrInfo, markovNet.size());
        for (int i = 0; i < markovNet.size(); i++) {
            Instance instance = new DenseInstance(markovNet.size());
            for (int j = 0; j < markovNet.size(); j++) {
                if (i != j) {
                    Double value = markovNet.get(i).connectedNode.get(markovNet.get(j));
                    if (value != null)
                        instance.setValue(j, value);
                    else
                        instance.setValue(j, 0);
                } else {
                    instance.setValue(j, 0);
                }
            }
            for (int j = 0; j < markovNet.get(i).pattern.length; j++) {
                instance.setValue(markovNet.size() + j, markovNet.get(i).pattern[j]);
            }
            data.add(instance);
        }
        try {
            NormalizableDistance dist = new ChebyshevDistance();
            dist.setDontNormalize(true);
            dist.setAttributeIndices("1-" + markovNet.size());
            clusterer.setDistanceFunction(dist);
            clusterer.buildClusterer(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void normalize() {
        for (MarkovNode node : markovNet) {
            double sumTrans = node.connectedNode.values().stream().reduce((result, item) -> result + item).get();
            for (MarkovNode transNode : node.connectedNode.keySet()) {
                node.connectedNode.put(transNode, node.connectedNode.get(transNode) / sumTrans);
            }
        }
    }

    public double[] process(byte[] input) {
        NormalizableDistance dist = new ManhattanDistance();
        dist.setDontNormalize(true);
        dist.setAttributeIndices((markovNet.size() + 1) + "-" + (markovNet.size() + 1 + input.length));
        clusterer.setDistanceFunction(dist);

        Instance instance = new DenseInstance(markovNet.size());
        for (int j = 0; j < input.length; j++) {
            instance.setValue(markovNet.size() + j, input[j]);
        }

        try {
            return clusterer.distributionForInstance(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        double[] result = new double[temporalGroups.size()];
//        double maxClosest = 0;
//        for (int i = 0; i < temporalGroups.size(); i++) {
//            double closest = Double.MAX_VALUE;
//            for (MarkovNode node : temporalGroups.get(i).nodes) {
//                double dist = IntStream.range(0, node.pattern.length)
//                        .map(index -> Math.abs(node.pattern[index] - input[index]))
//                        .sum() / (input.length * 255);
//                if (dist < closest)
//                    closest = dist;
//            }
//            closest = Math.exp(-closest * closest / SIGMA);
//            if (closest > maxClosest)
//                maxClosest = closest;
//            result[i] = closest;
//        }
//        for (int i = 0; i < temporalGroups.size(); i++) {
//            result[i] /= maxClosest;
//        }
        return null;
    }

    private class MarkovNode {
        int index;
        byte[] pattern;
        Map<MarkovNode, Double> connectedNode = new HashMap<>();

        private MarkovNode(int index, byte[] pattern) {
            this.index = index;
            this.pattern = pattern;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MarkovNode that = (MarkovNode) o;

            return index == that.index;
        }

        @Override
        public int hashCode() {
            return index;
        }
    }

}
