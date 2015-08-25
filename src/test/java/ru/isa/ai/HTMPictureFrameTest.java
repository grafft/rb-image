package ru.isa.ai;

import ru.isa.ai.htm.HTMNetwork;
import ru.isa.ai.utils.MNISTDatasetReader;
import ru.isa.ai.utils.MovieUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Author: Aleksandr Panov
 * Date: 17.11.2014
 * Time: 14:13
 */
public class HTMPictureFrameTest {
    private static final int LEVEL_AMOUNT = 3;

    private static int currentItem = 0;
    private static double[] currentImage;
    private static int currentLevel = 0;
    private static byte[] labels;
    private static byte[][] images;
    private static HTMNetwork htmPicture;

    public static void main(String[] args) throws IOException, URISyntaxException {
        URL resource = HTMPictureTest.class.getClassLoader().getResource("pictures/packages/20x1/train-labels.idx1-ubyte");
        assert resource != null;

        File testFile = new File(resource.toURI().getPath());
        MNISTDatasetReader reader = new MNISTDatasetReader(testFile.getParentFile().getPath());

        images = reader.readData();
        currentImage = MovieUtils.toDouble(images[0]);
        labels = reader.getLabels();

        htmPicture = new HTMNetwork();

        SwingUtilities.invokeLater(HTMPictureFrame::new);
    }

    private static class HTMPictureFrame extends JFrame {
        public HTMPictureFrame() throws HeadlessException {
            super("HTM Picture");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            getContentPane().setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(3, 3, 3, 3);
            c.anchor = GridBagConstraints.FIRST_LINE_START;

            InfoPanel infoPanel = new InfoPanel();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.1;
            c.weighty = 1.0;
            c.gridx = 0;
            c.gridy = 0;
            getContentPane().add(infoPanel, c);

            NodePanel nodePanel = new NodePanel();
            c.anchor = GridBagConstraints.PAGE_START;
            c.weightx = 0.9;
            c.weighty = 1.0;
            c.gridx = 1;
            c.gridy = 0;
            c.fill = GridBagConstraints.BOTH;
            getContentPane().add(nodePanel, c);

            pack();
            setSize(new Dimension(900, 600));
            setVisible(true);
        }
    }

    private static class InfoPanel extends JPanel {
        public InfoPanel() {
            setBorder(LineBorder.createBlackLineBorder());

            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(3, 3, 3, 3);
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            c.fill = GridBagConstraints.HORIZONTAL;

            JButton nextMovieButton = new JButton("Next image");
            final JLabel imageIndexLabel = new JLabel("" + currentItem);
            final ImagePreviewPanel imagePanel = new ImagePreviewPanel();
            final JButton startMovieButton = new JButton("Show movie");
            final JTextField learnAmountField = new JTextField("10000");
            learnAmountField.setInputVerifier(new NumberVerifier());
            learnAmountField.setToolTipText("Learn amount");
            final JTextField testAmountField = new JTextField("10000");
            testAmountField.setInputVerifier(new NumberVerifier());
            testAmountField.setToolTipText("Test amount");

            final JButton totalLearnButton = new JButton("Total learning");
            final JButton startLearnButton = new JButton("Start learning");
            final JLabel learningLevelLabel = new JLabel("" + currentLevel);
            final JButton startRecognitionButton = new JButton("Recognize");
            startRecognitionButton.setEnabled(false);
            final JLabel recognitionLabel = new JLabel("");
            final JButton getStatisticButton = new JButton("Get statistic");
            getStatisticButton.setEnabled(false);
            final JLabel statisticLabel = new JLabel("");

            c.weightx = 0.5;
            c.gridx = 0;
            c.gridwidth = 2;
            c.gridy = 0;
            nextMovieButton.addActionListener(e -> {
                currentItem++;
                imageIndexLabel.setText("" + currentItem);
                currentImage = MovieUtils.toDouble(images[currentItem]);
                imagePanel.repaint();
            });
            add(nextMovieButton, c);

            c.weightx = 0.5;
            c.gridx = 2;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.CENTER;
            add(imageIndexLabel, c);

            c.weightx = 1.0;
            c.gridx = 0;
            c.gridwidth = 3;
            c.gridy = 1;
            startMovieButton.addActionListener(e -> {
                final double[][] movie = MovieUtils.createVerticalMovieFull(images[currentItem]);
                ((JButton) e.getSource()).setEnabled(false);
                Timer timer = new Timer(10, new ActionListener() {
                    int counter = 0;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (counter < movie.length) {
                            currentImage = movie[counter];
                            imagePanel.repaint();
                            counter++;
                        } else {
                            ((Timer) e.getSource()).stop();
                            startMovieButton.setEnabled(true);
                        }

                    }
                });
                timer.start();
            });
            add(startMovieButton, c);

            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = 2;
            c.gridwidth = 2;
            c.anchor = GridBagConstraints.CENTER;
            add(learnAmountField, c);

            c.weightx = 0.5;
            c.gridx = 2;
            c.gridy = 2;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.CENTER;
            add(testAmountField, c);

            c.weightx = 0.5;
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy = 3;
            totalLearnButton.addActionListener(e -> {
                new Thread(() -> {
                    ((JButton) e.getSource()).setEnabled(false);
                    startLearnButton.setEnabled(false);
                    int amount = Integer.parseInt(learnAmountField.getText());
                    for (int i = 0; i < LEVEL_AMOUNT - 1; i++) {
                        while (currentItem < (currentLevel + 1) * amount) {
                            htmPicture.learnLevel(i, MovieUtils.createHorizontalMovieFull(images[currentItem]));
                            htmPicture.learnLevel(i, MovieUtils.createVerticalMovieFull(images[currentItem]));

                            currentItem++;
                            currentImage = MovieUtils.toDouble(images[currentItem]);
                            imagePanel.repaint();
                            imageIndexLabel.setText("" + currentItem);
                        }
                        htmPicture.finalizeLevelLearning(currentLevel);
                        currentLevel++;
                        learningLevelLabel.setText("" + currentLevel);
                    }
                    while (currentItem < (currentLevel + 1) * amount) {
                        htmPicture.learningAll(MovieUtils.createHorizontalMovieFull(images[currentItem]), labels[currentItem]);
                        htmPicture.learningAll(MovieUtils.createVerticalMovieFull(images[currentItem]), labels[currentItem]);

                        currentItem++;
                        currentImage = MovieUtils.toDouble(images[currentItem]);
                        imagePanel.repaint();
                        imageIndexLabel.setText("" + currentItem);
                    }
                    htmPicture.prepare();
                    startRecognitionButton.setEnabled(true);
                    getStatisticButton.setEnabled(true);
                }).start();
            });
            add(totalLearnButton, c);

            c.weightx = 0.5;
            c.gridwidth = 1;
            c.gridx = 1;
            c.gridy = 3;
            startLearnButton.addActionListener(e -> {
                new Thread(() -> {
                    ((JButton) e.getSource()).setEnabled(false);
                    int amount = Integer.parseInt(learnAmountField.getText());
                    while (currentItem < (currentLevel + 1) * amount) {
                        currentImage = MovieUtils.toDouble(images[currentItem]);
                        imagePanel.repaint();
                        if (currentLevel < LEVEL_AMOUNT - 1) {
                            htmPicture.learnLevel(currentLevel, MovieUtils.createHorizontalMovieFull(images[currentItem]));
                            htmPicture.learnLevel(currentLevel, MovieUtils.createVerticalMovieFull(images[currentItem]));
                        } else {
                            htmPicture.learningAll(MovieUtils.createHorizontalMovieFull(images[currentItem]), labels[currentItem]);
                            htmPicture.learningAll(MovieUtils.createVerticalMovieFull(images[currentItem]), labels[currentItem]);
                        }
                        currentItem++;
                        imageIndexLabel.setText("" + currentItem);
                    }
                    if (currentLevel < LEVEL_AMOUNT - 1) {
                        htmPicture.finalizeLevelLearning(currentLevel);
                        currentLevel++;
                        learningLevelLabel.setText("" + currentLevel);
                        ((JButton) e.getSource()).setEnabled(true);
                    } else {
                        htmPicture.prepare();
                        learningLevelLabel.setText("top");
                        startRecognitionButton.setEnabled(true);
                        getStatisticButton.setEnabled(true);
                    }
                }).start();
            });
            add(startLearnButton, c);

            c.weightx = 0.5;
            c.gridwidth = 1;
            c.gridx = 2;
            c.gridy = 3;
            c.anchor = GridBagConstraints.CENTER;
            add(learningLevelLabel, c);

            c.weightx = 0.5;
            c.gridwidth = 2;
            c.gridx = 0;
            c.gridy = 4;
            startRecognitionButton.addActionListener(e -> {
                byte result = htmPicture.recognize(MovieUtils.toDouble(images[currentItem]));
                recognitionLabel.setText("" + result);
                recognitionLabel.setForeground(result == labels[currentItem] ? Color.black : Color.red);
            });
            add(startRecognitionButton, c);

            c.weightx = 0.5;
            c.gridwidth = 1;
            c.gridx = 2;
            c.gridy = 4;
            c.anchor = GridBagConstraints.CENTER;
            add(recognitionLabel, c);

            c.weightx = 0.5;
            c.gridwidth = 2;
            c.gridx = 0;
            c.gridy = 5;
            getStatisticButton.addActionListener(e -> {
                ((JButton) e.getSource()).setEnabled(false);
                int amount = Integer.parseInt(testAmountField.getText());
                new Thread(() -> {
                    int counter = 0;
                    double correct = 0;
                    while (counter < amount) {
                        currentImage = MovieUtils.toDouble(images[currentItem]);
                        imagePanel.repaint();
                        byte result = htmPicture.recognize(MovieUtils.toDouble(images[currentItem]));
                        recognitionLabel.setText("" + result);
                        if (result == labels[currentItem]) {
                            correct++;
                            recognitionLabel.setForeground(Color.black);
                        } else {
                            recognitionLabel.setForeground(Color.red);
                        }
                        currentItem++;
                        imageIndexLabel.setText("" + currentItem);
                        counter++;
                    }
                    ((JButton) e.getSource()).setEnabled(true);
                    statisticLabel.setText(String.format("%3.2f", correct / amount));
                }).start();
            });
            add(getStatisticButton, c);

            c.weightx = 0.5;
            c.gridwidth = 1;
            c.gridx = 2;
            c.gridy = 5;
            c.anchor = GridBagConstraints.CENTER;
            add(statisticLabel, c);

            c.weightx = 1.0;
            c.ipady = 200;
            c.gridx = 0;
            c.gridwidth = 3;
            c.gridy = 6;
            add(imagePanel, c);
        }
    }

    private static class NodePanel extends JPanel {
        public NodePanel() {
            setBorder(LineBorder.createBlackLineBorder());
        }
    }

    private static class ImagePreviewPanel extends JPanel {
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
                    int gray = 255 - (int) currentImage[i * (int) imageSize + j];
                    g2.setPaint(new Color(gray, gray, gray));
                    g2.fill(new Rectangle2D.Double(j * cellWidth, i * cellHeight, cellWidth, cellHeight));
                }
            }
        }
    }

    private static class NumberVerifier extends InputVerifier {

        @Override
        public boolean verify(JComponent input) {
            String text = ((JTextField) input).getText();
            try {
                int value = Integer.parseInt(text);
                return (value < images.length / 4);
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}
