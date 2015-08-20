package ru.isa.ai;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import ru.isa.ai.utils.IOMapper;

import java.util.Arrays;

/**
 * Author: Aleksandr Panov
 * Date: 03.02.2015
 * Time: 17:48
 */
public class IOMapperTest extends TestCase {

    public IOMapperTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(IOMapperTest.class);
    }

    public void test7x7to3x3() {
        IOMapper mapper = new IOMapper(3, 3, 3, 3);
        double[][] testInput = {{0, 1, 2, 3, 4, 5, 6},
                {7, 8, 9, 10, 11, 12, 13},
                {14, 15, 16, 17, 18, 19, 20},
                {21, 22, 23, 24, 25, 26, 27},
                {28, 29, 30, 31, 32, 33, 34},
                {35, 36, 37, 38, 39, 40, 41},
                {42, 43, 44, 45, 46, 47, 48}};

        double[] result1 = mapper.map(testInput, 0, 0);
        assertTrue(Arrays.equals(result1, new double[]{0, 1, 2, 7, 8, 9, 14, 15, 16}));

        double[] result2 = mapper.map(testInput, 1, 1);
        assertTrue(Arrays.equals(result2, new double[]{16, 17, 18, 23, 24, 25, 30, 31, 32}));

        double[] result3 = mapper.map(testInput, 1, 2);
        assertTrue(Arrays.equals(result3, new double[]{30, 31, 32, 37, 38, 39, 44, 45, 46}));
    }

    public void test7x7to2x2() {
        IOMapper mapper = new IOMapper(4, 4, 2, 2);
        double[][] testInput = {{0, 1, 2, 3, 4, 5, 6},
                {7, 8, 9, 10, 11, 12, 13},
                {14, 15, 16, 17, 18, 19, 20},
                {21, 22, 23, 24, 25, 26, 27},
                {28, 29, 30, 31, 32, 33, 34},
                {35, 36, 37, 38, 39, 40, 41},
                {42, 43, 44, 45, 46, 47, 48}};

        double[] result1 = mapper.map(testInput, 0, 0);
        assertTrue(Arrays.equals(result1, new double[]{0, 1, 2, 3, 7, 8, 9, 10, 14, 15, 16, 17, 21, 22, 23, 24}));

        double[] result2 = mapper.map(testInput, 1, 1);
        assertTrue(Arrays.equals(result2, new double[]{24, 25, 26, 27, 31, 32, 33, 34, 38, 39, 40, 41, 45, 46, 47, 48}));
    }

    public void test6x6to3x3() {
        IOMapper mapper = new IOMapper(3, 3, 2, 2);
        double[][] testInput = {{0, 1, 2, 3, 4, 5},
                {6, 7, 8, 9, 10, 11},
                {12, 13, 14, 15, 16, 17},
                {18, 19, 20, 21, 22, 23},
                {24, 25, 26, 27, 28, 29},
                {30, 31, 32, 33, 34, 35}};

        double[] result1 = mapper.map(testInput, 0, 0);
        assertTrue(Arrays.equals(result1, new double[]{0, 1, 2, 6, 7, 8, 12, 13, 14}));

        double[] result2 = mapper.map(testInput, 1, 1);
        assertTrue(Arrays.equals(result2, new double[]{21, 22, 23, 27, 28, 29, 33, 34, 35}));
    }

    public void test3x3to1x1() {
        IOMapper mapper = new IOMapper(3, 3, 1, 1);
        double[][] testInput = {{0, 1, 2},
                {3, 4, 5},
                {6, 7, 8}};

        double[] result1 = mapper.map(testInput, 0, 0);
        assertTrue(Arrays.equals(result1, new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8}));
    }
}
