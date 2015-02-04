package ru.isa.ai;

import ru.isa.ai.eik.IKNetwork;

import java.io.IOException;
import java.util.Random;

/**
 * Author: Aleksandr Panov
 * Date: 03.02.2015
 * Time: 18:55
 */
public class IKNetworkTest {
    private static Random rand = new Random();

    private static final double[][] BLANK = {{0, 0, 0},
            {0, 0, 0},
            {0, 0, 0}};
    private static final double[][] VERT_1 = {{1, 0, 0},
            {1, 0, 0},
            {1, 0, 0}};
    private static final double[][] VERT_2 = {{0, 1, 0},
            {0, 1, 0},
            {0, 1, 0}};
    private static final double[][] VERT_3 = {{0, 0, 1},
            {0, 0, 1},
            {0, 0, 1}};

    private static final double[][] GOR_1 = {{1, 1, 1},
            {0, 0, 0},
            {0, 0, 0}};
    private static final double[][] GOR_2 = {{0, 0, 0},
            {1, 1, 1},
            {0, 0, 0}};
    private static final double[][] GOR_3 = {{0, 0, 0},
            {0, 0, 0},
            {1, 1, 1}};

    public static void main(String[] args) throws IOException {
        IKNetwork network = new IKNetwork(IKNetworkTest.class.getClassLoader().getResource("ike_test_1.properties").getPath());

        for (int i = 0; i < 3000; i++) {
            double[][] next = getNextImage(i);
            double[][] noisy = createNoisy(next);

            network.process(noisy);
        }
    }

    private static double[][] getNextImage(int i) {
        switch (i % 18) {
            case 0:
            case 1:
            case 2:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 15:
            case 16:
            case 17:
                return BLANK;
            case 3:
                return VERT_1;
            case 4:
                return VERT_2;
            case 5:
                return VERT_3;
            case 12:
                return GOR_1;
            case 13:
                return GOR_2;
            case 14:
                return GOR_3;
            default:
                return BLANK;
        }
    }

    private static double[][] createNoisy(double[][] input) {
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                input[i][j] = input[i][j] + rand.nextGaussian() * 0.1;
                input[i][j] = input[i][j] < 0 ? 0 : (input[i][j] > 1 ? 1 : input[i][j]);
            }
        }

        return input;
    }
}
