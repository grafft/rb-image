package ru.isa.ai.utils;

/**
 * Author: Aleksandr Panov
 * Date: 17.11.2014
 * Time: 14:11
 */
public class MovieUtils {
    public static double[][] createHMovieForNode(double[] image, int nodeAmount, int nodeIndex) {
        int imageSize = (int) Math.sqrt(image.length);
        int nodeSize = imageSize / nodeAmount;
        double[][] movie = new double[nodeAmount][nodeSize * nodeSize];
        for (int i = 0; i < nodeAmount; i++) {
            for (int j = 0; j < nodeSize; j++) {
                for (int k = 0; k < nodeSize; k++) {
                    movie[i][j * nodeSize + k] = image[(nodeIndex * nodeSize + j) * imageSize + i * nodeSize + k];
                }
            }
        }
        return movie;
    }

    public static double[][] createVMovieForNode(double[] image, int nodeAmount, int nodeIndex) {
        int imageSize = (int) Math.sqrt(image.length);
        int nodeSize = imageSize / nodeAmount;
        double[][] movie = new double[nodeAmount][nodeSize * nodeSize];
        for (int i = 0; i < nodeAmount; i++) {
            for (int j = 0; j < nodeSize; j++) {
                for (int k = 0; k < nodeSize; k++) {
                    movie[i][j * nodeSize + k] = image[(i * nodeSize + j) * imageSize + nodeIndex * nodeSize + k];
                }
            }
        }
        return movie;
    }

    public static double[][] createHorizontalMovie(byte[] image) {
        int size = (int) Math.sqrt(image.length);
        double[][] movie = new double[2 * size][image.length];
        for (int shift = -size; shift < size; shift++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    movie[shift + size][j * size + k] = (k + shift < size && k + shift >= 0) ?
                            ((0xFF & image[j * size + k + shift]) > 128 ? 1 : 0) : 0;
                }
            }
        }
        return movie;
    }

    public static double[][] createHorizontalMovieFull(byte[] image) {
        int size = (int) Math.sqrt(image.length);
        double[][] movie = new double[2 * size][image.length];
        for (int shift = -size; shift < size; shift++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    movie[shift + size][j * size + k] = (k + shift < size && k + shift >= 0) ?
                            (0xFF & image[j * size + k + shift]) : 0;
                }
            }
        }
        return movie;
    }

    public static double[][] createVerticalMovie(byte[] image) {
        int size = (int) Math.sqrt(image.length);
        double[][] movie = new double[2 * size][image.length];
        for (int shift = -size; shift < size; shift++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    movie[shift + size][j * size + k] = (j + shift < size && j + shift >= 0) ?
                            ((0xFF & image[(j + shift) * size + k]) > 128 ? 1 : 0) : 0;
                }
            }
        }
        return movie;
    }

    public static double[][] createVerticalMovieFull(byte[] image) {
        int size = (int) Math.sqrt(image.length);
        double[][] movie = new double[2 * size][image.length];
        for (int shift = -size; shift < size; shift++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    movie[shift + size][j * size + k] = (j + shift < size && j + shift >= 0) ?
                            (0xFF & image[(j + shift) * size + k]) : 0;
                }
            }
        }
        return movie;
    }

    public static double[] negative(byte[] positive) {
        double[] result = new double[positive.length];
        for (int i = 0; i < positive.length; i++)
            result[i] = (0xFF & positive[i]) > 128 ? 1 : 0;
        return result;
    }

    public static double[] toDouble(byte[] positive) {
        double[] result = new double[positive.length];
        for (int i = 0; i < positive.length; i++)
            result[i] = 0xFF & positive[i];
        return result;
    }

    public static String imageToString(byte[] image) {
        int size = (int) Math.sqrt(image.length);
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < size; j++) {
            for (int k = 0; k < size; k++) {
                builder.append((0xFF & image[j * size + k]) > 128 ? "x" : " ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    public static String imageToString(double[] image) {
        int size = (int) Math.sqrt(image.length);
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < size; j++) {
            for (int k = 0; k < size; k++) {
                builder.append((image[j * size + k]) > 128 ? "x" : " ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
