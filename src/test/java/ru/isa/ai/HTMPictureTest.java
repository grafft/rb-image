package ru.isa.ai;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.isa.ai.htm.HTMNetwork;
import ru.isa.ai.utils.MNISTDatasetReader;

import java.io.File;
import java.io.IOException;

/**
 * Author: Aleksandr Panov
 * Date: 23.10.2014
 * Time: 11:36
 */
public class HTMPictureTest {
    private static final Logger logger = LogManager.getLogger(HTMPictureTest.class.getSimpleName());

    public static final int SIZE = 500;

    public static void main(String[] args) throws IOException {
        File testFile = new File(HTMPictureTest.class.getClassLoader().getResource("train-labels-idx1-ubyte.gz").getPath());
        MNISTDatasetReader reader = new MNISTDatasetReader(testFile.getParentFile().getPath());

        byte[][] images = reader.readData();
        byte[] labels = reader.getLabels();

        HTMNetwork htmPicture = new HTMNetwork();
        logger.info(String.format("Start learning in 0 level"));
        for (int i = 0; i < SIZE; i++) {
            htmPicture.learnLevel(0, createHorizontalMovie(images[i]));
            htmPicture.learnLevel(0, createVerticalMovie(images[i]));
        }
        logger.info(String.format("Finalize learning in 0 level"));
        htmPicture.finalizeLevelLearning(0);

        logger.info(String.format("Start learning in 1 level"));
        for (int i = SIZE; i < 2 * SIZE; i++) {
            htmPicture.learnLevel(1, createHorizontalMovie(images[i]));
            htmPicture.learnLevel(1, createVerticalMovie(images[i]));
        }
        logger.info(String.format("Finalize learning 1 level"));
        htmPicture.finalizeLevelLearning(1);

        logger.info(String.format("Start all learning"));
        for (int i = 2 * SIZE; i < 3 * SIZE; i++) {
            htmPicture.learningAll(createHorizontalMovie(images[i]), labels[i]);
            htmPicture.learningAll(createVerticalMovie(images[i]), labels[i]);
        }

        logger.info(String.format("Finalize all learning"));
        htmPicture.prepare();

        logger.info(String.format("Start recognition test"));
        for (int i = 0; i < 15; i++) {
            int check = (int) (3 * SIZE + Math.random() * SIZE);
            byte result = htmPicture.recognize(negative(images[check]));
            logger.info("Classify image:\n" + imageToString(images[check]) + "as " + result + " when was " + labels[check]);
        }
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

    public static double[] negative(byte[] positive) {
        double[] result = new double[positive.length];
        for (int i = 0; i < positive.length; i++)
            result[i] = (0xFF & positive[i]) > 128 ? 1 : 0;
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
}
