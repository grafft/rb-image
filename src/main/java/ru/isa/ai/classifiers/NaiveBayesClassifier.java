package ru.isa.ai.classifiers;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: Aleksandr Panov
 * Date: 22.10.2014
 * Time: 16:25
 */
public class NaiveBayesClassifier {
    private Map<String, List<String>> examples = new HashMap<>();
    private Map<String, Integer> docsCount = new HashMap<>();
    private Map<String, Map<String, Integer>> wordsCount = new HashMap<>();
    private Map<String, Integer> totalWordsCount = new HashMap<>();
    private int totalDocs;
    private int totalWords;

    public void addExample(String text, String clazz) {
        if (!examples.containsKey(clazz))
            examples.put(clazz, new ArrayList<>());
        examples.get(clazz).add(text);
        totalDocs++;
    }

    public void buildModel() {
        Set<String> words = new HashSet<>();

        for (Map.Entry<String, List<String>> entry : examples.entrySet()) {
            docsCount.put(entry.getKey(), entry.getValue().size());
            Map<String, Integer> wordMap = new HashMap<>();
            int wordCount = 0;
            for (String oldText : entry.getValue()) {
                for (String word : oldText.split(" ")) {
                    words.add(word);
                    wordCount++;
                    if (!wordMap.containsKey(word))
                        wordMap.put(word, 1);
                    else
                        wordMap.put(word, wordMap.get(word) + 1);
                }
            }
            wordsCount.put(entry.getKey(), wordMap);
            totalWordsCount.put(entry.getKey(), wordCount);
        }
        totalWords = words.size();
    }

    public String classify(String newText) {
        Map<String, Double> probabilities = new HashMap<>();
        for (String word : newText.split(" ")) {
            for (String clazz : docsCount.keySet()) {
                if (!probabilities.containsKey(clazz))
                    probabilities.put(clazz, Math.log(docsCount.get(clazz) / (totalDocs + 0.0)));
                double temp = wordsCount.get(clazz).get(word) == null ? 0 : wordsCount.get(clazz).get(word);
                probabilities.put(clazz, probabilities.get(clazz) + Math.log((temp + 1) / (totalWords + totalWordsCount.get(clazz))));
            }
        }
        String result = probabilities.entrySet().stream().sorted((o1, o2) -> -o1.getValue().compareTo(o2.getValue())).
                findFirst().get().getKey();
        final double expSum = probabilities.values().stream().reduce(0.0, (res, item) -> res + Math.exp(item));

        Map<String, Double> newMap = probabilities.entrySet().stream().
                collect(Collectors.toMap(Map.Entry::getKey, entry -> (Math.exp(entry.getValue()) / expSum)));
        System.out.println(newMap.entrySet().stream().map(entry -> String.format("%s->%f", entry.getKey(), entry.getValue())).
                collect(Collectors.joining("; ")));
        return result;
    }

}
