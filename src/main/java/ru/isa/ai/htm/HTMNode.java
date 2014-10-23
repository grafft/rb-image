package ru.isa.ai.htm;

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

    private List<MarkovNode> markovNet = new ArrayList<>();
    private List<List<MarkovNode>> temporalGroups = new ArrayList<>();
    private MarkovNode previous = null;

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
                previous.connectedNode.put(current, 1);
            else
                previous.connectedNode.put(current, previous.connectedNode.get(current) + 1);
        }
        previous = current;
    }

    public void generateTemporalGroups() {
        normalize();

        for (MarkovNode node : markovNet) {

        }

    }

    private void normalize() {
        for (MarkovNode node : markovNet) {
            int maxTrans = 0;
            for (int trans : node.connectedNode.values()) {
                if (maxTrans < trans)
                    maxTrans = trans;
            }
            for (MarkovNode transNode : node.connectedNode.keySet()) {
                node.connectedNode.put(transNode, node.connectedNode.get(transNode) / maxTrans);
            }
        }
    }

    public double[] process(byte[] input) {
        double[] result = new double[temporalGroups.size()];
        double maxClosest = 0;
        for (int i = 0; i < temporalGroups.size(); i++) {
            double closest = Double.MAX_VALUE;
            for (MarkovNode node : temporalGroups.get(i)) {
                double dist = IntStream.range(0, node.pattern.length)
                        .map(index -> Math.abs(node.pattern[index] - input[index]))
                        .sum() / (input.length * 255);
                if (dist < closest)
                    closest = dist;
            }
            closest = Math.exp(-closest * closest / SIGMA);
            if (closest > maxClosest)
                maxClosest = closest;
            result[i] = closest;
        }
        for (int i = 0; i < temporalGroups.size(); i++) {
            result[i] /= maxClosest;
        }
        return result;
    }

    private class MarkovNode {
        int index;
        byte[] pattern;
        Map<MarkovNode, Integer> connectedNode = new HashMap<>();

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
