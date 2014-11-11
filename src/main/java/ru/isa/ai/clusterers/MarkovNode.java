package ru.isa.ai.clusterers;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Aleksandr Panov
 * Date: 11.11.2014
 * Time: 17:13
 */
public class MarkovNode {
    private int index;
    private double[] pattern;
    private Map<MarkovNode, Double> connectedNode = new HashMap<>();

    public MarkovNode(int index, double[] pattern) {
        this.index = index;
        this.pattern = pattern;
    }

    public int getIndex() {
        return index;
    }

    public double[] getPattern() {
        return pattern;
    }

    public void setPattern(double[] pattern) {
        this.pattern = pattern;
    }

    public Map<MarkovNode, Double> getConnectedNode() {
        return connectedNode;
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
