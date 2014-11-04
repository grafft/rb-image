package ru.isa.ai.htm;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Aleksandr Panov
 * Date: 31.10.2014
 * Time: 19:06
 */
public class ClusteredMarkovNode extends MarkovNode{
    private double[] fullPattern;

    public ClusteredMarkovNode(int index, double[] pattern) {
        super(index, new byte[]{});
        this.fullPattern = pattern;
    }

    public double[] getFullPattern() {
        return fullPattern;
    }


    public void setFullPattern(double[] pattern) {
        this.fullPattern = pattern;
    }
}
