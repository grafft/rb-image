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
        int index = (int) (Math.random() * images.length);
        byte[][] movie = createHorizontalMovie(images[index]);

        HTMPicture htmPicture = new HTMPicture();
        htmPicture.learnMovie(movie, labels[index]);

        assert labels[index] == htmPicture.recognize(movie[0]);
    }

    public static byte[][] createHorizontalMovie(byte[] image) {
        int size = (int) Math.sqrt(image.length);
        byte[][] movie = new byte[image.length][2 * size];
        for (int shift = -size; shift < size; shift++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    movie[shift + size][j * size + k] = (k + shift < size || k + shift >= 0) ? (byte) (0xFF & image[j * size + k + shift]) : 0;
                }
            }
        }
        return movie;
    }
}
