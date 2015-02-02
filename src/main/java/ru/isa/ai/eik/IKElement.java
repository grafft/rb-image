package ru.isa.ai.eik;

import ru.isa.ai.clusterers.spatial.SOMSpatialClusterer;
import ru.isa.ai.clusterers.temporal.RSOMTemporalClusterer;
import ru.isa.ai.utils.StringUtils;

import java.util.Properties;

/**
 * Author: Aleksandr Panov
 * Date: 02.02.2015
 * Time: 19:54
 */
public class IKElement {
    public static final String SPAT_DIM_PROP = "spat_input.dimension";
    public static final String TEMP_DIM_PROP = "temp_input.dimension";
    public static final String SPAT_GROW_PROP = "spat.grow_rate";
    public static final String TEMP_GROW_PROP = "temp.grow_rate";
    private SOMSpatialClusterer spatialClusterer;
    private RSOMTemporalClusterer temporalClusterer;

    private Properties properties;

    public IKElement(Properties properties, int layer, int index) {
        this.properties = properties;

        int spDim = Integer.parseInt(properties.getProperty(SPAT_DIM_PROP + "." + layer + "." + index));
        int tempDim = Integer.parseInt(properties.getProperty(TEMP_DIM_PROP + "." + layer + "." + index));
        int[] spGrow = StringUtils.parseIntArray(properties.getProperty(SPAT_GROW_PROP + "." + layer + "." + index));
        int[] tempGrow = StringUtils.parseIntArray(properties.getProperty(TEMP_GROW_PROP + "." + layer + "." + index));

        spatialClusterer = new SOMSpatialClusterer(spDim, spGrow);
        temporalClusterer = new RSOMTemporalClusterer(tempDim, tempGrow);
    }

    private double[] process(double[] input) {
        double[] temp = spatialClusterer.process(input);
        double[] output = temporalClusterer.process(temp);

        return output;
    }
}
