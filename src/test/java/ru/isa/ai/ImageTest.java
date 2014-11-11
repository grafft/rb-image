package ru.isa.ai;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import ru.isa.ai.rb_simple.RBHierarchy;

/**
 * Unit test for simple App.
 */
public class ImageTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ImageTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(ImageTest.class);
    }

    public void testImageRB() {
        RBHierarchy rbh = new RBHierarchy(3, new int[]{10, 100, 1000});
        rbh.addRBToLevel(0, 10, 10);

    }
}
