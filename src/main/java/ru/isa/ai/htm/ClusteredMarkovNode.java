package ru.isa.ai.htm;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Aleksandr Panov
 * Date: 31.10.2014
 * Time: 19:06
 */
public class ClusteredMarkovNode {
    private int index;
    private double[] pattern;
    private Map<ClusteredMarkovNode, Double> connectedNode = new HashMap<>();

    public ClusteredMarkovNode(int index, double[] pattern) {
        this.index = index;
        this.pattern = pattern;
    }

    public int getIndex() {
        return index;
    }

    public double[] getPattern() {
        return pattern;
    }

    public Map<ClusteredMarkovNode, Double> getConnectedNode() {
        return connectedNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClusteredMarkovNode that = (ClusteredMarkovNode) o;

        return index == that.index;
    }

    @Override
    public int hashCode() {
        return index;
    }
}
