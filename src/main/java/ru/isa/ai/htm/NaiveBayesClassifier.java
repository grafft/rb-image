package ru.isa.ai.htm;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: Aleksandr Panov
 * Date: 29.10.2014
 * Time: 12:57
 */
public class NaiveBayesClassifier {
    private Map<Byte, List<byte[]>> examples = new HashMap<>();
    private Map<Byte, Integer> patternCount = new HashMap<>();
    private Map<Byte, Map<Integer, Integer>> bitsCount = new HashMap<>();
    private Map<Byte, Integer> totalBitsCount = new HashMap<>();
    private int totalPatterns;
    private int totalBits;

    public void addExample(byte[] pattern, byte clazz) {
        if (!examples.containsKey(clazz))
            examples.put(clazz, new ArrayList<>());
        examples.get(clazz).add(pattern);
        totalPatterns++;
    }

    public void buildModel() {
        for (Map.Entry<Byte, List<byte[]>> entry : examples.entrySet()) {
            patternCount.put(entry.getKey(), entry.getValue().size());
            Map<Integer, Integer> bitMap = new HashMap<>();
            int bitCount = 0;
            for (byte[] oldPattern : entry.getValue()) {
                if(totalBits < oldPattern.length)
                    totalBits = oldPattern.length;
                for (int i = 0; i < oldPattern.length; i++) {
                    if (oldPattern[i] > 0) {
                        bitCount++;
                        if (!bitMap.containsKey(i))
                            bitMap.put(i, 1);
                        else
                            bitMap.put(i, bitMap.get(i) + 1);
                    }
                }
            }
            bitsCount.put(entry.getKey(), bitMap);
            totalBitsCount.put(entry.getKey(), bitCount);
        }
    }

    public byte classify(byte[] pattern) {
        Map<Byte, Double> probabilities = new HashMap<>();
        for (int i = 0; i < pattern.length; i++) {
            if (pattern[i] > 0) {
                for (byte clazz : patternCount.keySet()) {
                    if (!probabilities.containsKey(clazz))
                        probabilities.put(clazz, Math.log(patternCount.get(clazz) / (totalPatterns + 0.0)));
                    double temp = bitsCount.get(clazz).get(i) == null ? 0 : bitsCount.get(clazz).get(i);
                    probabilities.put(clazz, probabilities.get(clazz) + Math.log((temp + 1) / (totalBits + totalBitsCount.get(clazz))));
                }
            }
        }
        Byte result = probabilities.entrySet().stream().sorted((o1, o2) -> -o1.getValue().compareTo(o2.getValue())).
                findFirst().get().getKey();
        final double expSum = probabilities.values().stream().reduce(0.0, (res, item) -> res + Math.exp(item));

        Map<Byte, Double> newMap = probabilities.entrySet().stream().
                collect(Collectors.toMap(Map.Entry::getKey, entry -> (Math.exp(entry.getValue()) / expSum)));
        System.out.println(newMap.entrySet().stream().map(entry -> String.format("%s->%f", entry.getKey(), entry.getValue())).
                collect(Collectors.joining("; ")));
        return result;
    }
}
