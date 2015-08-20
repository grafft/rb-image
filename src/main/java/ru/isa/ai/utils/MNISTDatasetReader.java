package ru.isa.ai.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

/**
 * Author: Aleksandr Panov
 * Date: 23.10.2014
 * Time: 11:37
 */
public class MNISTDatasetReader {
    private byte[] labels;
    private byte[][] images;

    private String directoryPath;

    public MNISTDatasetReader(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public byte[][] readData() throws IOException {
        System.out.println("READING STARTED");
        //BufferedInputStream labelStream = new BufferedInputStream(new GZIPInputStream(
        //        new FileInputStream(directoryPath + File.separator + "train-labels-idx1-ubyte.gz")));
        BufferedInputStream labelStream = new BufferedInputStream(
                new FileInputStream(directoryPath + File.separator + "train-labels.idx1-ubyte"));
        byte[] info = new byte[8];
        if (labelStream.read(info, 0, 8) == 8) {
            int magicNumber = 0;
            int itemsAmount = 0;
            for (int i = 0; i < 4; i++)
            {
                magicNumber += info[i] * Math.pow(256, 3-i);
                System.out.print(info[i]);
            }
            for (int i = 4; i < 8; i++)
            {
                itemsAmount += info[i] * Math.pow(256, 7-i);
                System.out.print(info[i]);
            }
            System.out.println(String.format("Magic number: %d, number of items: %d", magicNumber, itemsAmount));

            labels = new byte[itemsAmount];
            int readed = labelStream.read(labels, 0, itemsAmount);
            System.out.println(String.format("Readed: %d", readed));
            if (readed != itemsAmount) {
                throw new IOException("Could not read info");
            }
            labelStream.close();

            BufferedInputStream imageStream = new BufferedInputStream(
                    new FileInputStream(directoryPath + File.separator + "train-images.idx3-ubyte"));
            byte[] imageInfo = new byte[16];
            imageStream.read(imageInfo, 0, 16);
            magicNumber = 0;
            for (int i = 0; i < 4; i++)
            {
                magicNumber += imageInfo[i] * Math.pow(256, 3-i);
                System.out.print(imageInfo[i]);
            }
            int imagesAmount = 0;
            for (int i = 4; i < 8; i++)
            {
                imagesAmount += imageInfo[i] * Math.pow(256, 7-i);
                System.out.print(imageInfo[i]);
            }
            if (itemsAmount != imagesAmount) {
                throw new IOException("Other amount of items");
            }
            int rowsAmount = 0;
            for (int i = 8; i < 12; i++)
            {
                rowsAmount += imageInfo[i] * Math.pow(256, 11-i);
                System.out.print(imageInfo[i]);
            }
            int columnsAmount = 0;
            for (int i = 12; i < 16; i++)
            {
                columnsAmount += imageInfo[i] * Math.pow(256, 15-i);
                System.out.print(imageInfo[i]);
            }
            System.out.println(String.format("Magic number: %d, number of items: %d, rows: %d, columns: %d", magicNumber, itemsAmount, rowsAmount, columnsAmount));
            System.out.println(String.format("Readed: %d", readed));
            images = new byte[itemsAmount][];
            for (int i = 0; i < itemsAmount; i++) {
                byte[] imageData = new byte[rowsAmount * columnsAmount];
                readed = imageStream.read(imageData, 0, rowsAmount * columnsAmount);
                if (readed != rowsAmount * columnsAmount) {
                    throw new IOException("Could not read image " + i + " of " + readed);
                }
                images[i] = imageData;
            }
            imageStream.close();
        } else {
            throw new IOException("Could not read info");
        }

        return images;
    }

    public byte[] getLabels() {
        return labels;
    }

    public byte[][] getImages() {
        return images;
    }
}
