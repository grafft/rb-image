package ru.isa.ai;

import ru.isa.ai.utils.MNISTDatasetReader;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

/**
 * Author: Aleksandr Panov
 * Date: 17.10.2014
 * Time: 14:28
 */
public class MNISTDatasetReaderTest {
    private static int currentItem = 0;
    private static byte[] data;
    private static byte[][] images;

    public static void main(String[] args) throws IOException {
        File testFile = new File(MNISTDatasetReaderTest.class.getClassLoader().getResource("train-labels-idx1-ubyte.gz").getPath());
        MNISTDatasetReader reader = new MNISTDatasetReader(testFile.getParentFile().getPath());

        images = reader.readData();
        data = reader.getLabels();
        SwingUtilities.invokeLater(MNISTFrame::new);
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
                    if (e.getKeyChar() == '\n') {
                        try {
                            int item = Integer.parseInt(numberField.getText());
                            if (item < data.length) {
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
            stepButton.addActionListener(e -> {
                try {
                    int item = Integer.parseInt(numberField.getText());
                    if (item < data.length) {
                        currentItem = item;
                        infoLabel.setText("" + data[currentItem]);
                        imagePanel.repaint();
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Invalid number format");
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

            double imageSize = Math.sqrt(images[0].length);
            double cellWidth = size.getWidth() / imageSize;
            double cellHeight = size.getHeight() / imageSize;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int gray = 255 - (0xFF & images[currentItem][i * (int) imageSize + j]);
                    g2.setPaint(new Color(gray, gray, gray));
                    g2.fill(new Rectangle2D.Double(j * cellWidth, i * cellHeight, cellWidth, cellHeight));
                }
            }
        }
    }
}
