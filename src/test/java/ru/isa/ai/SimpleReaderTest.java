package ru.isa.ai;

import ru.isa.ai.utils.MNISTDatasetReader;
import ru.isa.ai.utils.MovieUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by Aleksandr Panov on 20.08.2015.
 */
public class SimpleReaderTest {
    public static void main(String[] args) throws URISyntaxException, IOException {
        URL resource = HTMPictureTest.class.getClassLoader().getResource("pictures/packages/20x1/train-labels.idx1-ubyte");

        File testFile = new File(resource.toURI().getPath());
        MNISTDatasetReader reader = new MNISTDatasetReader(testFile.getParentFile().getPath());

        byte[][] images = reader.readData();

        double[] currentImage = MovieUtils.toDouble(images[0]);

        int imageSize = (int) Math.sqrt(images[0].length);
        for (int i = 0; i < imageSize; i++) {
            for (int j = 0; j < imageSize; j++) {
                System.out.print((int)currentImage[i * imageSize + j] + " ");
            }
            System.out.println();
        }
    }
}
