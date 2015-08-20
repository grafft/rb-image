package ru.isa.ai.eik;

import ru.isa.ai.utils.IOMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Author: Aleksandr Panov
 * Date: 02.02.2015
 * Time: 20:12
 */
public class IKNetwork {
    public static final String LAYERS_PROP = "network.layers";
    public static final String LAYER_X_PROP = "network.x_number";
    public static final String LAYER_Y_PROP = "network.y_number";
    public static final String RECEPT_X_PROP = "receptive.x_number";
    public static final String RECEPT_Y_PROP = "receptive.y_number";

    private Map<Integer, IKElement> elements = new TreeMap<>();
    private Map<Integer, List<Integer>> layers = new TreeMap<>();

    private Map<Integer, IOMapper> layerMappers = new HashMap<>();

    public IKNetwork(Properties properties) {
        int layersNumber = Integer.parseInt(properties.getProperty(LAYERS_PROP));
        int index = 0;
        for (int i = 0; i < layersNumber; i++) {
            List<Integer> layerIndexes = new ArrayList<>();
            int xCount = Integer.parseInt(properties.getProperty(LAYER_X_PROP + "." + i));
            int yCount = Integer.parseInt(properties.getProperty(LAYER_Y_PROP + "." + i));
            for (int j = 0; j < xCount * yCount; j++) {
                elements.put(index, new IKElement(properties, i, index));
                layerIndexes.add(index);
                index++;
            }
            layers.put(i, layerIndexes);

            int xDim = Integer.parseInt(properties.getProperty(RECEPT_X_PROP + "." + i));
            int yDim = Integer.parseInt(properties.getProperty(RECEPT_Y_PROP + "." + i));

            layerMappers.put(i, new IOMapper(xDim, yDim, xCount, yCount));
        }
    }

    public void process(double[][] squareInput) {
        double[][] layerInput = squareInput;
        for (int key : layers.keySet()) {
            int counter = 0;
            for (int index : layers.get(key)) {
                IOMapper mapper = layerMappers.get(key);
                int y = counter / mapper.getOutXCount();
                int x = counter - y * mapper.getOutXCount();

                double[] result = elements.get(index).process(mapper.map(layerInput, x, y));
                counter++;
            }
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int key : layers.keySet()) {
            builder.append("Layer ").append(key).append(":\n");
            for (int index : layers.get(key)) {
                builder.append("\tElement ").append(index).append(":\n");
                builder.append("\t\t").append(elements.get(index).toString()).append("\n");
            }
        }
        return builder.toString();
    }

    public Map<Integer, IKElement> getElements() {
        return elements;
    }
}
