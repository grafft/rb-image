package ru.isa.ai.htm;

/**
 * Author: Aleksandr Panov
 * Date: 23.10.2014
 * Time: 11:13
 */
public class HTMPicture {
    private HTMNode[] firstLevel = new HTMNode[7 * 7];
    private HTMNode[] secondLevel = new HTMNode[4 * 4];
    private HTMNode topNode = new HTMNode();

    public HTMPicture() {
        for (int i = 0; i < 7 * 7; i++)
            firstLevel[i] = new HTMNode();
        for (int i = 0; i < 4 * 4; i++)
            secondLevel[i] = new HTMNode();
    }

    public void learnMovie(byte[][] movie, byte label) {
        for (int i = 0; i < 28 * 2; i++) {
            byte[] image = movie[i];
            learnFirstLevel(image);
        }
        for (int i = 0; i < 7 * 7; i++) {
            firstLevel[i].generateTemporalGroups();
        }
    }

    public byte recognize(byte[] image) {
        byte result = 0;
        for (int i = 0; i < 7 * 7; i++) {
            byte[] input = new byte[4 * 4];
            int startY = i / 7;
            int startX = i - i / 7;
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    input[j * 4 + k] = image[(startY + j) * 28 + startX + k];
                }
            }
            firstLevel[i].process(input);
        }
        return result;
    }

    private void learnFirstLevel(byte[] image) {
        for (int i = 0; i < 7 * 7; i++) {
            byte[] input = new byte[4 * 4];
            int startY = i / 7;
            int startX = i - i / 7;
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    input[j * 4 + k] = image[(startY + j) * 28 + startX + k];
                }
            }
            firstLevel[i].learn(input);
        }
    }
}
