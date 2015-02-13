package ru.isa.ai;

import org.apache.commons.lang3.ArrayUtils;
import ru.isa.ai.eik.IKNetwork;
import ru.isa.ai.utils.MathUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Author: Aleksandr Panov
 * Date: 03.02.2015
 * Time: 18:55
 */
public class IKNetworkTest {
    private static final String NOISY_DEV_PROP = "noisy_dev";
    private static IKNetwork network;
    private static int counter = 0;
    private static double[][] currentImage;
    private static double noisyDeviation;
    private static Random rand = new Random();

    private static final double[][] BLANK = {{0, 0, 0},
            {0, 0, 0},
            {0, 0, 0}};
    private static final double[][] VERT_1 = {{1, 0, 0},
            {1, 0, 0},
            {1, 0, 0}};
    private static final double[][] VERT_2 = {{0, 1, 0},
            {0, 1, 0},
            {0, 1, 0}};
    private static final double[][] VERT_3 = {{0, 0, 1},
            {0, 0, 1},
            {0, 0, 1}};

    private static final double[][] GOR_1 = {{1, 1, 1},
            {0, 0, 0},
            {0, 0, 0}};
    private static final double[][] GOR_2 = {{0, 0, 0},
            {1, 1, 1},
            {0, 0, 0}};
    private static final double[][] GOR_3 = {{0, 0, 0},
            {0, 0, 0},
            {1, 1, 1}};

    public static void main(String[] args) throws IOException {
        String propPath = IKNetworkTest.class.getClassLoader().getResource("ike_test_1.properties").getPath();
        Properties prop = new Properties();
        prop.load(new FileInputStream(propPath));
        network = new IKNetwork(prop);
        noisyDeviation = Double.parseDouble(prop.getProperty(NOISY_DEV_PROP));
//        for (int i = 0; i < 3000; i++) {
//            double[][] next = getNextImage(i);
//            double[][] noisy = createNoisy(next);
//
//            network.process(noisy);
//        }

        System.out.println(network.toString());
        SwingUtilities.invokeLater(ROMStateFrame::new);
    }

    private static double[][] getNextImage(int i) {
        switch (i % 18) {
            case 0:
            case 1:
            case 2:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 15:
            case 16:
            case 17:
                return BLANK;
            case 3:
                return VERT_1;
            case 4:
                return VERT_2;
            case 5:
                return VERT_3;
            case 12:
                return GOR_1;
            case 13:
                return GOR_2;
            case 14:
                return GOR_3;
            default:
                return BLANK;
        }
    }

    private static double[][] createNoisy(double[][] input) {
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                input[i][j] = input[i][j] == 0 ? rand.nextGaussian() * noisyDeviation : input[i][j];
                input[i][j] = input[i][j] < 0 ? 0 : (input[i][j] > 1 ? 1 : input[i][j]);
            }
        }

        return input;
    }

    private static class ROMStateFrame extends JFrame {
        public ROMStateFrame() {
            super("ROM State Frame");
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setLayout(new GridBagLayout());
            StatePanel statePanel = new StatePanel();
            getContentPane().add(statePanel, new GridBagConstraints(0, 1, 1, 1, 1, 0.97,
                    GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

            getContentPane().add(new JPanel() {
                {
                    JButton stepButton = new JButton("Step");
                    JButton step10Button = new JButton("Step 10");
                    JButton step100Button = new JButton("Step 100");
                    JLabel label = new JLabel("" + counter);
                    stepButton.addActionListener(e -> {
                        double[][] next = getNextImage(counter);
                        currentImage = createNoisy(next);

                        network.process(currentImage);
                        counter++;
                        statePanel.repaint();
                        label.setText("" + counter);
                    });
                    add(stepButton);
                    step10Button.addActionListener(e -> {
                        for (int i = 0; i < 10; i++) {
                            double[][] next = getNextImage(counter);
                            currentImage = createNoisy(next);

                            network.process(currentImage);
                            counter++;
                        }
                        statePanel.repaint();
                        label.setText("" + counter);
                    });
                    add(step10Button);
                    step100Button.addActionListener(e -> {
                        for (int i = 0; i < 100; i++) {
                            double[][] next = getNextImage(counter);
                            currentImage = createNoisy(next);

                            network.process(currentImage);
                            counter++;
                        }
                        statePanel.repaint();
                        label.setText("" + counter);
                    });
                    add(step100Button);
                    add(label);
                    setBorder(BorderFactory.createLineBorder(Color.black, 1));
                }


            }, new GridBagConstraints(0, 0, 1, 1, 1, 0.03,
                    GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

            pack();
            setSize(new Dimension(800, 850));
            setVisible(true);
        }
    }

    private static class StatePanel extends JPanel {
        public StatePanel() {
            setBorder(LineBorder.createBlackLineBorder());
        }

        public void paint(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int startX = 50;
            int startY = 50;
            g2.drawString("SOM units:", 30, 40);
            double[][] spaNeurons = network.getElements().get(0).getSpatialClusterer().getNeurons();
            for (int i = 0; i < spaNeurons.length; i++) {
                if (spaNeurons[i] != null) {
                    drawUnit(g2, startX, startY, spaNeurons[i],
                            network.getElements().get(0).getSpatialClusterer().getCurrentBMU() == i);
                }
                startX += 80;
            }

            g2.setPaint(Color.BLACK);
            g2.drawString("RSOM units:", 30, 140);
            startX = 50;
            startY = 150;
            double[][] tempNeurons = network.getElements().get(0).getTemporalClusterer().getNeurons();
            for (int i = 0; i < tempNeurons.length; i++) {
                if (tempNeurons[i] != null) {
                    Map<Double, Integer> map = new TreeMap<>((o1, o2) -> -Double.compare(o1, o2));
                    for (int j = 0; j < tempNeurons[i].length; j++) {
                        map.put(tempNeurons[i][j], j);
                    }
                    int cnt = 0;
                    for (int index : map.values()) {
                        g2.setPaint(Color.BLACK);
                        g2.drawString(String.format("%.3f", tempNeurons[i][index]), startX + i * 70, startY + cnt * 70 + 30);
                        drawUnit(g2, startX + i * 70 + 30, startY + cnt * 70,
                                network.getElements().get(0).getSpatialClusterer().getNeurons()[index],
                                network.getElements().get(0).getTemporalClusterer().getCurrentBMU() == i);
                        cnt++;
                    }
                }
                startX += 80;
            }

            if (currentImage != null)
                drawUnit(g2, 600, 300, MathUtils.flatten(currentImage), false);
        }

    }

    private static void drawUnit(Graphics2D g2, int startX, int startY, double[] neuron, boolean isBMU) {
        if (neuron != null) {
            g2.setPaint(isBMU ? Color.RED : Color.BLACK);
            g2.drawRect(startX, startY, 60, 60);
            for (int j = 0; j < neuron.length; j++) {
                int gray = (int) (255 * (1.0 - neuron[j]));
                g2.setPaint(new Color(gray, gray, gray));
                g2.fillRect(startX + (j - 3 * (j / 3)) * 20 + 1, startY + (j / 3) * 20 + 1, 19, 19);
            }
        }
    }
}
