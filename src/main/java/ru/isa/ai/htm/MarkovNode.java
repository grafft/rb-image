package ru.isa.ai.htm;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Aleksandr Panov
 * Date: 29.10.2014
 * Time: 12:44
 */
public class MarkovNode {
    private int index;
    private byte[] pattern;
    private Map<MarkovNode, Double> connectedNode = new HashMap<>();

    public MarkovNode(int index, byte[] pattern) {
        this.index = index;
        this.pattern = pattern;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public byte[] getPattern() {
        return pattern;
    }

    public void setPattern(byte[] pattern) {
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