package ru.isa.ai;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

/**
 * Author: Aleksandr Panov
 * Date: 17.10.2014
 * Time: 14:28
 */
public class ReadMNISTDataset {
    private static int itemsAmount;
    private static int currentItem = 0;

    private static int rowsAmount;
    private static int columnsAmount;
    private static byte[] data;
    private static byte[][] images;

    public static void main(String[] args) throws IOException {
        BufferedInputStream labelStream = new BufferedInputStream(new GZIPInputStream(ReadMNISTDataset.class.getClassLoader().getResourceAsStream("train-labels-idx1-ubyte.gz")));
        byte[] info = new byte[4 * 2];
        if (labelStream.read(info, 0, 4 * 2) == 4 * 2) {
            ByteBuffer infoBuffer = ByteBuffer.wrap(info);
            int magicNumber = infoBuffer.getInt();
            itemsAmount = infoBuffer.getInt();
            System.out.println(String.format("Magic number: %d, number of items: %d", magicNumber, itemsAmount));

            data = new byte[itemsAmount];
            int readed = labelStream.read(data, 0, itemsAmount);
            if (readed != itemsAmount) {
                System.out.println("Could not read info");
                return;
            }
            labelStream.close();

            BufferedInputStream imageStream = new BufferedInputStream(new GZIPInputStream(ReadMNISTDataset.class.getClassLoader().getResourceAsStream("train-images-idx3-ubyte.gz")));
            byte[] imageInfo = new byte[4 * 4];
            imageStream.read(imageInfo, 0, 4 * 4);
            ByteBuffer imageInfoBuffer = ByteBuffer.wrap(imageInfo);
            magicNumber = imageInfoBuffer.getInt();
            if (itemsAmount != imageInfoBuffer.getInt()) {
                System.out.println("Other amount of items");
                return;
            }
            rowsAmount = imageInfoBuffer.getInt();
            columnsAmount = imageInfoBuffer.getInt();
            System.out.println(String.format("Magic number: %d, number of items: %d, rows: %d, columns: %d", magicNumber, itemsAmount, rowsAmount, columnsAmount));

            images = new byte[itemsAmount][];
            for (int i = 0; i < itemsAmount; i++) {
                byte[] imageData = new byte[rowsAmount * columnsAmount];
                readed = imageStream.read(imageData, 0, rowsAmount * columnsAmount);
                if (readed != rowsAmount * columnsAmount) {
                    System.out.println("Could not read image " + i + ", " + readed);
                    return;
                }
                images[i] = imageData;
            }
            imageStream.close();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new MNISTFrame();
                }
            });
        } else {
            System.out.println("Could not read info");
        }

    }

    private static class MNISTFrame extends JFrame {
        MNISTFrame() throws HeadlessException {
            super("MNIST Frame");

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

            getContentPane().setLayout(new GridBagLayout());
            JPanel infoPanel = new JPanel();
            Dimension size = new Dimension(100, 300);
            infoPanel.setMaximumSize(size);
            infoPanel.setPreferredSize(size);
            infoPanel.setMinimumSize(size);
            infoPanel.setLayout(new GridLayout(10, 1, 7, 7));
            infoPanel.setBorder(LineBorder.createBlackLineBorder());

            final JTextField numberField = new JTextField("0");
            final JButton stepButton = new JButton("Show");
            final JLabel infoLabel = new JLabel("" + data[currentItem]);

            infoPanel.add(numberField);
            infoPanel.add(stepButton);
            infoPanel.add(infoLabel);


            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHEAST;
            c.insets = new Insets(3, 3, 3, 3);
            getContentPane().add(infoPanel, c);

            final ImagePanel imagePanel = new ImagePanel();
            c.anchor = GridBagConstraints.NORTH;
            c.fill = GridBagConstraints.BOTH;
            c.weighty = 1.0;
            c.weightx = 1.0;
            c.gridy = 0;
            getContentPane().add(imagePanel, c);

            numberField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    super.keyTyped(e);
                    if(e.getKeyChar() == '\n'){
                        try {
                            int item = Integer.parseInt(numberField.getText());
                            if (item < itemsAmount) {
                                currentItem = item;
                                infoLabel.setText("" + data[currentItem]);
                                imagePanel.repaint();
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println("Invalid number format");
                        }
                    }
                }
            });
            stepButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        int item = Integer.parseInt(numberField.getText());
                        if (item < itemsAmount) {
                            currentItem = item;
                            infoLabel.setText("" + data[currentItem]);
                            imagePanel.repaint();
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid number format");
                    }
                }
            });

            pack();
            setSize(new Dimension(400, 300));
            setVisible(true);
        }
    }

    private static class ImagePanel extends JPanel {
        public void paint(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Dimension size = getSize();

            double cellWidth = size.getWidth() / columnsAmount;
            double cellHeight = size.getHeight() / rowsAmount;
            for (int i = 0; i < rowsAmount; i++) {
                for (int j = 0; j < columnsAmount; j++) {
                    int gray = 255 - (0xFF & images[currentItem][i * columnsAmount + j]);
                    g2.setPaint(new Color(gray, gray, gray));
                    g2.fill(new Rectangle2D.Double(j * cellWidth, i * cellHeight, cellWidth, cellHeight));
                }
            }
        }
    }
}
