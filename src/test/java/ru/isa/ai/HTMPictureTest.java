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
    public static void main(String[] args) throws IOException {
        File testFile = new File(HTMPictureTest.class.getClassLoader().getResource("train-labels-idx1-ubyte.gz").getPath());
        MNISTDatasetReader reader = new MNISTDatasetReader(testFile.getParentFile().getPath());

        byte[][] images = reader.readData();
        byte[] labels = reader.getLabels();

        HTMPicture htmPicture = new HTMPicture();
        for (int i = 0; i < 100; i++) {
            htmPicture.learnMovie(createHorizontalMovie(images[i]), labels[i]);
            htmPicture.learnMovie(createVerticalMovie(images[i]), labels[i]);
        }

        htmPicture.finalizeLearning();
        int result = htmPicture.recognize(images[1001]);
        assert (labels[1001] == result);
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
}
