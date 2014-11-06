package ru.isa.ai.htm;

import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Author: Aleksandr Panov
 * Date: 29.10.2014
 * Time: 12:57
 */
public class NaiveBayesDoubleClassifier {
    private Instances examples;
    private NaiveBayes nbc = new NaiveBayes();

    public NaiveBayesDoubleClassifier(int length) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            attributes.add(new Attribute("attr_" + i, i));
        }
        attributes.add(new Attribute("class", length));
        this.examples = new Instances("NBC", attributes, 100);
        examples.setClassIndex(length);
    }

    public void addExample(double[] pattern, byte clazz) {
        Instance instance = new DenseInstance(0, pattern);
        instance.setDataset(examples);
        instance.setClassValue(clazz);
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
            Instance instance = new DenseInstance(0, pattern);
            double[] distribution = nbc.distributionForInstance(instance);
            result = (byte) nbc.classifyInstance(instance);
            System.out.println(Arrays.toString(distribution));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
