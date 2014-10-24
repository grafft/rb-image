package ru.isa.ai.htm;

import java.util.Arrays;

/**
 * Author: Aleksandr Panov
 * Date: 23.10.2014
 * Time: 11:13
 */
public class HTMPicture {
    public static final int INPUT_SIZE = 28;
    public static final int DIMENSION_1 = 7;
    public static final int DIMENSION_2 = 4;
    public static final int OUTPUT_SIZE_1 = 10;
    public static final int OUTPUT_SIZE_2 = 5;

    private HTMNode[] firstLevel = new HTMNode[DIMENSION_1 * DIMENSION_1];
    private HTMNode[] secondLevel = new HTMNode[DIMENSION_2 * DIMENSION_2];
    private HTMNode topNode = new HTMNode();

    public HTMPicture() {
        for (int i = 0; i < DIMENSION_1 * DIMENSION_1; i++) {
            firstLevel[i] = new HTMNode();
            firstLevel[i].setMaxTGNumber(OUTPUT_SIZE_1);
        }
        for (int i = 0; i < DIMENSION_2 * DIMENSION_2; i++) {
            secondLevel[i] = new HTMNode();
            secondLevel[i].setMaxTGNumber(OUTPUT_SIZE_2);
        }
    }

    public void learnMovie1(byte[][] movie) {
        // learn first level
        for (byte[] aMovie : movie) {
            for (int j = 0; j < DIMENSION_1 * DIMENSION_1; j++) {
                byte[] input = firstLevelCut(aMovie, j);
                firstLevel[j].learn(input);
            }
        }
    }

    public void normalize1() {
        for (int i = 0; i < DIMENSION_1 * DIMENSION_1; i++) {
            firstLevel[i].generateTemporalGroups();
        }
    }

    public void learnMovie2(byte[][] movie) {
        // learn second level
        for (byte[] aMovie : movie) {
            byte[] input = firstRecognize(aMovie);

            for (int j = 0; j < DIMENSION_2 * DIMENSION_2; j++) {
                byte[] cutInput = secondLevelCut(input, j);
                secondLevel[j].learn(cutInput);
            }
        }
    }

    public void normalize2() {
        for (int i = 0; i < DIMENSION_2 * DIMENSION_2; i++) {
            secondLevel[i].generateTemporalGroups();
        }
    }

    public void learnMovie3(byte[][] movie, byte label) {
        // learn top
        for (byte[] aMovie : movie) {
            byte[] input = firstRecognize(aMovie); // 10 * 7 * 7 length
            byte[] input2 = secondRecognize(input); // 10 * 4 * 4 length

            topNode.learn(input2);
        }
    }

    private byte[] firstLevelCut(byte[] image, int index) {
        int size = INPUT_SIZE / DIMENSION_1;
        byte[] input = new byte[size * size];
        int startY = index / DIMENSION_1;
        int startX = index - index / DIMENSION_1;
        for (int j = 0; j < size; j++) {
            for (int k = 0; k < size; k++) {
                input[j * size + k] = image[(startY + j) * INPUT_SIZE + startX + k];
            }
        }
        return input;
    }

    public byte[] firstRecognize(byte[] image) {
        byte[] output = new byte[OUTPUT_SIZE_1 * DIMENSION_1 * DIMENSION_1];
        for (int i = 0; i < DIMENSION_1 * DIMENSION_1; i++) {
            byte[] input = firstLevelCut(image, i);
            double[] result = firstLevel[i].process(input);
            double max = Arrays.stream(result).max().getAsDouble();
            for (int j = 0; j < OUTPUT_SIZE_1; j++) {
                output[i * OUTPUT_SIZE_1 + j] = (byte) (result[j] < max ? 0 : 1);
            }
        }
        return output;
    }

    private byte[] secondLevelCut(byte[] input, int index) {
        int size = DIMENSION_1 / DIMENSION_2 + ((DIMENSION_1 % DIMENSION_2 == 0) ? 0 : 1);
        byte[] cutInput = new byte[size * size * OUTPUT_SIZE_1];
        for (int i = 0; i < size * size * OUTPUT_SIZE_1; i++) {
            cutInput[i] = (index * size * size * OUTPUT_SIZE_1 + i < DIMENSION_1 * DIMENSION_1 * OUTPUT_SIZE_1) ?
                    input[index * size * size * OUTPUT_SIZE_1 + i] : 0;
        }
        return cutInput;
    }

    private byte[] secondRecognize(byte[] input) {
        byte[] output = new byte[OUTPUT_SIZE_2 * DIMENSION_2 * DIMENSION_2];
        for (int i = 0; i < DIMENSION_2 * DIMENSION_2; i++) {
            byte[] cutInput = secondLevelCut(input, i);
            double[] result = secondLevel[i].process(cutInput);
            for (int j = 0; j < OUTPUT_SIZE_2; j++) {
                output[i * OUTPUT_SIZE_2 + j] = (byte) (result[j] > 0.5 ? 1 : 0);
            }
        }
        return output;
    }
}
