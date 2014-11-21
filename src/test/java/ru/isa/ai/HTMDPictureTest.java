package ru.isa.ai;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.isa.ai.htm.HTMDNetwork;
import ru.isa.ai.utils.MNISTDatasetReader;
import ru.isa.ai.utils.MovieUtils;

import java.io.File;
import java.io.IOException;

/**
 * Author: Aleksandr Panov
 * Date: 19.11.2014
 * Time: 17:19
 */
public class HTMDPictureTest {
    private static final Logger logger = LogManager.getLogger(HTMDPictureTest.class.getSimpleName());

    public static final int LEVEL_AMOUNT = 2;
    public static final int SIZE = 2000;
    private static final int TEST_SIZE = 1000;

    public static void main(String[] args) throws IOException {
        File testFile = new File(HTMPictureTest.class.getClassLoader().getResource("train-labels-idx1-ubyte.gz").getPath());
        MNISTDatasetReader reader = new MNISTDatasetReader(testFile.getParentFile().getPath());

        byte[][] images = reader.readData();
        byte[] labels = reader.getLabels();

        HTMDNetwork htmPicture = new HTMDNetwork(2, 28, 28,
                new int[]{2 * 7, 2*4}, //level node counts
                new int[]{7, 3}, // level times
                new int[]{9, 4}); // node outputs
        int currentItem = 0;

        for (int i = 0; i < LEVEL_AMOUNT; i++) {
            logger.info(String.format("Start learning in %d level", i));
            while (currentItem < (i + 1) * SIZE) {
                htmPicture.learnLevel(i, MovieUtils.toDouble(images[currentItem]));

                currentItem++;
            }
            htmPicture.finalizeLevelLearning(i);
        }
        logger.info(String.format("Start learning in top level"));
        while (currentItem < (LEVEL_AMOUNT + 1) * SIZE) {
            htmPicture.learningAll(MovieUtils.toDouble(images[currentItem]), labels[currentItem]);
            htmPicture.learningAll(MovieUtils.toDouble(images[currentItem]), labels[currentItem]);

            currentItem++;
        }
        logger.info(String.format("Start top preparation"));
        htmPicture.prepare();
        logger.info(String.format("Start recognition test"));
        double correct = 0;
        for (int i = 0; i < TEST_SIZE; i++) {
            int check = currentItem + i;
            byte result = htmPicture.recognize(MovieUtils.toDouble(images[check]));
            if (result == labels[check])
                correct++;
        }
        logger.info("Correctness: " + correct / TEST_SIZE);
    }
}
