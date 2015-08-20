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
        BufferedInputStream labelStream = new BufferedInputStream(new GZIPInputStream(
                new FileInputStream(directoryPath + File.separator + "train-labels-idx1-ubyte.gz")));
        byte[] info = new byte[4 * 2];
        if (labelStream.read(info, 0, 4 * 2) == 4 * 2) {
            ByteBuffer infoBuffer = ByteBuffer.wrap(info);
            int magicNumber = infoBuffer.getInt();
            int itemsAmount = infoBuffer.getInt();
            System.out.println(String.format("Magic number: %d, number of items: %d", magicNumber, itemsAmount));

            labels = new byte[itemsAmount];
            int readed = labelStream.read(labels, 0, itemsAmount);
            if (readed != itemsAmount) {
                throw new IOException("Could not read info");
            }
            labelStream.close();

            BufferedInputStream imageStream = new BufferedInputStream(new GZIPInputStream(
                    new FileInputStream(directoryPath + File.separator + "/train-images-idx3-ubyte.gz")));
            byte[] imageInfo = new byte[4 * 4];
            imageStream.read(imageInfo, 0, 4 * 4);
            ByteBuffer imageInfoBuffer = ByteBuffer.wrap(imageInfo);
            magicNumber = imageInfoBuffer.getInt();
            if (itemsAmount != imageInfoBuffer.getInt()) {
                throw new IOException("Other amount of items");
            }
            int rowsAmount = imageInfoBuffer.getInt();
            int columnsAmount = imageInfoBuffer.getInt();
            System.out.println(String.format("Magic number: %d, number of items: %d, rows: %d, columns: %d", magicNumber, itemsAmount, rowsAmount, columnsAmount));

            images = new byte[itemsAmount][];
            for (int i = 0; i < itemsAmount; i++) {
                byte[] imageData = new byte[rowsAmount * columnsAmount];
                readed = imageStream.read(imageData, 0, rowsAmount * columnsAmount);
                if (readed != rowsAmount * columnsAmount) {
                    throw new IOException("Could not read image " + i + ", " + readed);
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
