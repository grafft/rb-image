package ru.isa.ai.htm;

import ru.isa.ai.clusterers.temporal.MarkovNode;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Author: Aleksandr Panov
 * Date: 23.10.2014
 * Time: 11:14
 */
public class SimpleHTMNode extends AbstractHTMNode {

    protected SimpleHTMNode(int maxTGNumber) {
        super(maxTGNumber);
    }

    @Override
    protected double[] preProcessInput(double[] input) {
        double max = Arrays.stream(input).max().getAsDouble();
        return IntStream.range(0, input.length).mapToDouble(index -> input[index] < max ? 0.0 : 1.0).toArray();
    }

    @Override
    protected MarkovNode getCorrespondingNode(double[] pattern) {
        int found = IntStream.range(0, markovNet.size())
                .filter(index -> Arrays.equals(markovNet.get(index).getPattern(), pattern))
                .findAny().orElse(-1);
        if (found == -1) {
            found = markovNet.size();
            markovNet.add(new MarkovNode(found, pattern));
        }
        return markovNet.get(found);
    }

    @Override
    protected double getNodeDistance(MarkovNode node, double[] pattern) {
        double prob = 1;
        for (int i = 0; i < pattern.length; i++) {
            prob *= node.getPattern()[i] > 0 ? pattern[i] : 1;
        }
        return prob;
    }

}
