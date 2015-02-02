package ru.isa.ai.clusterers.temporal;

import ru.isa.ai.clusterers.RSOMClusterer;

/**
 * Author: Aleksandr Panov
 * Date: 02.02.2015
 * Time: 18:45
 */
public class RSOMTemporalClusterer extends RSOMClusterer{
    public RSOMTemporalClusterer(int inputDimension, int[] growthRate) {
        super(inputDimension, growthRate, 0.1);
    }
}
