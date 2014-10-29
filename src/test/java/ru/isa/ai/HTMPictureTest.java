package ru.isa.ai;

import ru.isa.ai.htm.HTMPicture;
import ru.isa.ai.htm.MNISTDatasetReader;

import java.io.File;
import java.io.IOException;

/**
 * Author: Aleksandr Panov
 * Date: 23.10.2014
 * Time: 11:36
 */
public class HTMPictureTest {
    public static final int SIZE = 100;

    public static void main(String[] args) throws IOException {
        File testFile = new File(HTMPictureTest.class.getClassLoader().getResource("train-labels-idx1-ubyte.gz").getPath());
        MNISTDatasetReader reader = new MNISTDatasetReader(testFile.getParentFile().getPath());

        byte[][] images = reader.readData();
        byte[] labels = reader.getLabels();

        HTMPicture htmPicture = new HTMPicture();
        for (int i = 0; i < SIZE; i++) {
            htmPicture.learnMovie1(createHorizontalMovie(images[i]));
            htmPicture.learnMovie1(createVerticalMovie(images[i]));
        }
        htmPicture.normalize1();
        for (int i = SIZE; i < 2 * SIZE; i++) {
            htmPicture.learnMovie2(createHorizontalMovie(images[i]));
            htmPicture.learnMovie2(createVerticalMovie(images[i]));
        }
        htmPicture.normalize2();

        for (int i = 2 * SIZE; i < 3 * SIZE; i++) {
            htmPicture.learnMovie3(createHorizontalMovie(images[i]), labels[i]);
            htmPicture.learnMovie3(createVerticalMovie(images[i]), labels[i]);
        }

        int check = (int) (Math.random() * 1000);
        byte result = htmPicture.recognize(negative(images[check]));
        System.out.println("Classify image:\n" + imageToString(images[check]) + "as " + result + " when was " + labels[check]);
    }

    public static byte[][] createHorizontalMovie(byte[] image) {
        int size = (int) Math.sqrt(image.length);
        byte[][] movie = new byte[2 * size][image.length];
        for (int shift = -size; shift < size; shift++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    movie[shift + size][j * size + k] = (k + shift < size && k + shift >= 0) ?
                            (byte) ((0xFF & image[j * size + k + shift]) > 128 ? 1 : 0) : 0;
                }
            }
        }
        return movie;
    }

    public static byte[][] createVerticalMovie(byte[] image) {
        int size = (int) Math.sqrt(image.length);
        byte[][] movie = new byte[2 * size][image.length];
        for (int shift = -size; shift < size; shift++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    movie[shift + size][j * size + k] = (j + shift < size && j + shift >= 0) ?
                            (byte) ((0xFF & image[(j + shift) * size + k]) > 128 ? 1 : 0) : 0;
                }
            }
        }
        return movie;
    }

    public static byte[] negative(byte[] positive) {
        byte[] result = new byte[positive.length];
        for (int i = 0; i < positive.length; i++)
            result[i] = (byte) ((0xFF & positive[i]) > 128 ? 1 : 0);
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
