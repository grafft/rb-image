package ru.isa.ai;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.isa.ai.htm.HTMNetwork;
import ru.isa.ai.utils.MNISTDatasetReader;
import ru.isa.ai.utils.MovieUtils;

import java.io.File;
import java.io.IOException;

/**
 * Author: Aleksandr Panov
 * Date: 23.10.2014
 * Time: 11:36
 */
public class HTMPictureTest {
    private static final Logger logger = LogManager.getLogger(HTMPictureTest.class.getSimpleName());

    public static final int SIZE = 1000;

    public static void main(String[] args) throws IOException {
        File testFile = new File(HTMPictureTest.class.getClassLoader().getResource("train-labels-idx1-ubyte.gz").getPath());
        MNISTDatasetReader reader = new MNISTDatasetReader(testFile.getParentFile().getPath());

        byte[][] images = reader.readData();
        byte[] labels = reader.getLabels();

        HTMNetwork htmPicture = new HTMNetwork();
        logger.info(String.format("Start learning in 0 level"));
        for (int i = 0; i < SIZE; i++) {
            htmPicture.learnLevel(0, MovieUtils.createHorizontalMovie(images[i]));
            htmPicture.learnLevel(0, MovieUtils.createVerticalMovie(images[i]));
        }
        logger.info(String.format("Finalize learning in 0 level"));
        htmPicture.finalizeLevelLearning(0);

        logger.info(String.format("Start learning in 1 level"));
        for (int i = SIZE; i < 2 * SIZE; i++) {
            htmPicture.learnLevel(1, MovieUtils.createHorizontalMovie(images[i]));
            htmPicture.learnLevel(1, MovieUtils.createVerticalMovie(images[i]));
        }
        logger.info(String.format("Finalize learning 1 level"));
        htmPicture.finalizeLevelLearning(1);

        logger.info(String.format("Start all learning"));
        for (int i = 2 * SIZE; i < 3 * SIZE; i++) {
            htmPicture.learningAll(MovieUtils.createHorizontalMovie(images[i]), labels[i]);
            htmPicture.learningAll(MovieUtils.createVerticalMovie(images[i]), labels[i]);
        }

        logger.info(String.format("Finalize all learning"));
        htmPicture.prepare();

        logger.info(String.format("Start recognition test"));
        for (int i = 0; i < 15; i++) {
            int check = (int) (3 * SIZE + Math.random() * SIZE);
            byte result = htmPicture.recognize(MovieUtils.negative(images[check]));
            logger.info("Classify image:\n" + MovieUtils.imageToString(images[check]) + "as " + result + " when was " + labels[check]);
        }
    }

}
