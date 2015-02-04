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
    public static final String SPAT_MAXOUT_PROP = "spat_output_max.dimension";
    public static final String TEMP_MAXOUT_PROP = "temp_output_max.dimension";
    public static final String SPAT_GROW_PROP = "spat.grow_rate";
    public static final String TEMP_GROW_PROP = "temp.grow_rate";

    public static final String SPAT_SIGMA_PROP = "spat.sigma";
    public static final String TEMP_SIGMA_PROP = "temp.sigma";

    private SOMSpatialClusterer spatialClusterer;
    private RSOMTemporalClusterer temporalClusterer;

    private Properties properties;

    public IKElement(Properties properties, int layer, int index) {
        this.properties = properties;

        int spDim = Integer.parseInt(properties.getProperty(SPAT_DIM_PROP + "." + layer));
        int tempDim = Integer.parseInt(properties.getProperty(TEMP_DIM_PROP + "." + layer));
        int spMaxOut = Integer.parseInt(properties.getProperty(SPAT_MAXOUT_PROP + "." + layer));
        int tempMaxOut = Integer.parseInt(properties.getProperty(TEMP_MAXOUT_PROP + "." + layer));
        int[] spGrow = StringUtils.parseIntArray(properties.getProperty(SPAT_GROW_PROP + "." + layer));
        int[] tempGrow = StringUtils.parseIntArray(properties.getProperty(TEMP_GROW_PROP + "." + layer));

        spatialClusterer = new SOMSpatialClusterer(spDim, spMaxOut, spGrow);
        spatialClusterer.setSigma(Integer.parseInt(properties.getProperty(SPAT_SIGMA_PROP + "." + layer)));
        temporalClusterer = new RSOMTemporalClusterer(tempDim, tempMaxOut, tempGrow);
        temporalClusterer.setSigma(Integer.parseInt(properties.getProperty(TEMP_SIGMA_PROP + "." + layer)));
    }

    public double[] process(double[] input) {
        double[] temp = spatialClusterer.process(input);
        double[] output = temporalClusterer.process(temp);

        return output;
    }
}
