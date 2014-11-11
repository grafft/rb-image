package ru.isa.ai.classifiers;

import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;

/** Weka Naive Bayes
 * Author: Aleksandr Panov
 * Date: 29.10.2014
 * Time: 12:57
 */
public class TopPatternClassifier {
    private Instances examples;
    private NaiveBayes nbc = new NaiveBayes();

    public TopPatternClassifier(int length) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            attributes.add(new Attribute("attr_" + i, i));
        }
        attributes.add(new Attribute("class", Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), length));
        this.examples = new Instances("NBC", attributes, 100);
        examples.setClassIndex(length);
    }

    public void addExample(double[] pattern, byte clazz) {
        double[] values = new double[pattern.length + 1];
        System.arraycopy(pattern, 0, values, 0, pattern.length);
        values[pattern.length] = clazz;
        Instance instance = new DenseInstance(1, values);
        examples.add(instance);
    }

    public void buildModel() {
        try {
            nbc.buildClassifier(examples);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte classify(double[] pattern) {
        byte result = -1;
        try {
            Instance instance = new DenseInstance(1, pattern);
            instance.setDataset(examples);
            double[] distribution = nbc.distributionForInstance(instance);
            result = (byte) nbc.classifyInstance(instance);
            System.out.println(Arrays.toString(distribution));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
