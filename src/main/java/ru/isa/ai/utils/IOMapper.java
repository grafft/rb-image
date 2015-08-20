package ru.isa.ai.utils;

/**
 * Author: Aleksandr Panov
 * Date: 03.02.2015
 * Time: 17:00
 */
public class IOMapper {
    private int outXDim;
    private int outYDim;
    private int outXCount;
    private int outYCount;

    public IOMapper(int outXDim, int outYDim, int outXCount, int outYCount) {
        this.outXDim = outXDim;
        this.outYDim = outYDim;
        this.outXCount = outXCount;
        this.outYCount = outYCount;
    }

    public double[] map(double[][] input, int indexX, int indexY) {
        double[] result = new double[outXDim * outYDim];
        double cellXPerItem = (input[0].length + 0.0) / outXCount;
        double cellYPerItem = (input.length + 0.0) / outYCount;

        int centerX = (int) Math.floor((indexX + 0.5) * cellXPerItem);
        int centerY = (int) Math.floor((indexY + 0.5) * cellYPerItem);

        int lowX = centerX - outXDim / 2;
        lowX = lowX < 0 ? 0 : lowX;
        int highX = centerX + outXDim / 2;
        highX = (highX - lowX + 1) > outXDim ? highX - 1 : highX;

        int lowY = centerY - outYDim / 2;
        lowY = lowY < 0 ? 0 : lowY;
        int highY = centerY + outXDim / 2;
        highY = (highY - lowY + 1) > outYDim ? highY - 1 : highY;


        int counter = 0;
        for (int i = lowY; i < highY + 1; i++) {
            for (int j = lowX; j < highX + 1; j++) {
                result[counter] = input[i][j];
                counter++;
            }
        }
        return result;
    }

    public int getOutXCount() {
        return outXCount;
    }

    public int getOutYCount() {
        return outYCount;
    }
}
