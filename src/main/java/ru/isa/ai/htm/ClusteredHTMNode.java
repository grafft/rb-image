package ru.isa.ai.htm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Author: Aleksandr Panov
 * Date: 31.10.2014
 * Time: 19:01
 */
public class ClusteredHTMNode {
    private int k = 50;
    private OnlineKMeansClusterer inputClusterer;
    protected List<ClusteredMarkovNode> markovNet = new ArrayList<>();
    protected ClusteredHTMNode previous = null;


    public ClusteredHTMNode(int k, int dimension) {
        inputClusterer = new OnlineKMeansClusterer(k, dimension);
        for (int i = 0; i < k; k++) {
            markovNet.add(new ClusteredMarkovNode(i, inputClusterer.getMeans()[i]));
        }
    }

    public void learn(byte[] input) {
        inputClusterer.updateClusterer(input);

    }
}
