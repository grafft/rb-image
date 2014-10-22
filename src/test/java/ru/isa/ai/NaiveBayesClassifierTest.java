package ru.isa.ai;

import ru.isa.ai.classifiers.NaiveBayesClassifier;

/**
 * Author: Aleksandr Panov
 * Date: 22.10.2014
 * Time: 16:26
 */
public class NaiveBayesClassifierTest {
    public static void main(String[] args) {
        NaiveBayesClassifier c = new NaiveBayesClassifier();
        c.addExample("предоставляю услуги бухгалтера", "SPAM");
        c.addExample("спешите купить виагру", "SPAM");
        c.addExample("надо купить молоко", "HAM");

        c.buildModel();
        System.out.println(c.classify("надо купить сигареты"));
    }
}
