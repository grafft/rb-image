package ru.isa.ai.eik;

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
    public static final String LAYER_PROP = "network.layers";
    private Map<Integer, IKElement> elements = new HashMap<>();
    private Map<Integer, List<Integer>> layers = new HashMap<>();

    public IKNetwork(String propFileName) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(propFileName));

        int layersNumber = Integer.parseInt(properties.getProperty(LAYERS_PROP));
        int index = 0;
        for (int i = 0; i < layersNumber; i++) {
            List<Integer> layerIndexes = new ArrayList<>();
            int elementNumber = Integer.parseInt(properties.getProperty(LAYER_PROP + "." + i));
            for (int j = 0; j < elementNumber; j++) {
                elements.put(index, new IKElement(properties, i, index));
                layerIndexes.add(index);
                index++;
            }
            layers.put(i, layerIndexes);
        }
    }

}
