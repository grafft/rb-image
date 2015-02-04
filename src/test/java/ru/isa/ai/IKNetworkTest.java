package ru.isa.ai;

import org.apache.commons.lang3.ArrayUtils;
import ru.isa.ai.eik.IKNetwork;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Author: Aleksandr Panov
 * Date: 03.02.2015
 * Time: 18:55
 */
public class IKNetworkTest {
    private static IKNetwork network;
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
        network = new IKNetwork(IKNetworkTest.class.getClassLoader().getResource("ike_test_1.properties").getPath());

        for (int i = 0; i < 3000; i++) {
            double[][] next = getNextImage(i);
            double[][] noisy = createNoisy(next);

            network.process(noisy);
        }

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
                input[i][j] = input[i][j] + rand.nextGaussian() * 0.01;
                input[i][j] = input[i][j] < 0 ? 0 : (input[i][j] > 1 ? 1 : input[i][j]);
            }
        }

        return input;
    }

    private static class ROMStateFrame extends JFrame {
        public ROMStateFrame() {
            super("ROM State Frame");
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            StatePanel statePanel = new StatePanel();
            getContentPane().add(statePanel);

            pack();
            setSize(new Dimension(900, 600));
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
            for (double[] spaNeuron : spaNeurons) {
                if (spaNeuron != null) {
                    drawUnit(g2, startX, startY, spaNeuron);
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
                    Arrays.sort(tempNeurons[i]);
                    ArrayUtils.reverse(tempNeurons[i]);
                    for (int j = 0; j < tempNeurons[i].length; j++) {
                        g2.setPaint(Color.BLACK);
                        g2.drawString(String.format("%.2f", tempNeurons[i][j]), startX + i * 70, startY + j * 70 + 30);
                        drawUnit(g2, startX + i * 70 + 30, startY + j * 70, network.getElements().get(0).getSpatialClusterer().getNeurons()[j]);
                    }
                }
                startX += 80;
            }
        }

        private void drawUnit(Graphics2D g2, int startX, int startY, double[] neuron) {
            g2.setPaint(Color.BLACK);
            g2.drawRect(startX, startY, 60, 60);
            for (int j = 0; j < neuron.length; j++) {
                int gray = (int) (255 * (1.0 - neuron[j]));
                g2.setPaint(new Color(gray, gray, gray));
                g2.fillRect(startX + (j - 3 * (j / 3)) * 20 + 1, startY + (j / 3) * 20 + 1, 19, 19);
            }
        }
    }
}
