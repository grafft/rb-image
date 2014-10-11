package ru.isa.ai.rb;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import cern.jet.math.tdouble.DoubleMult;
import cern.jet.math.tdouble.DoublePlusMultFirst;

import java.util.*;

/**
 * Created by GraffT on 09.10.2014.
 *
 */
public class RBHierarchy {
    private Map<Integer, List<RecognitionBlock>> hierarchy = new TreeMap<>();
    private Map<Integer, Integer> times = new TreeMap<>();
    private int totalT = 0;
    private int totalRB = 0;

    public RBHierarchy(int numLevels, int[] timeForLevels) {
        for (int i = 0; i < numLevels; i++) {
            hierarchy.put(i, new ArrayList<RecognitionBlock>());
            times.put(i, timeForLevels[i]);
        }
    }

    public void addRBToLevel(int level, int currentQ, double multiplier) {
        hierarchy.get(level).add(new RecognitionBlock(totalRB, level == 0 ? times.get(level) : times.get(level) / times.get(level - 1),
                currentQ, multiplier));
        totalRB++;
    }

    public void iterate(DoubleMatrix1D[] inputForFirstLevel) {
        for (int key : hierarchy.keySet()) {
            if (totalT % times.get(key) == 0) {
                Map<RecognitionBlock, DoubleMatrix1D> controlVectors = new HashMap<>();
                int counter = 0;
                for (RecognitionBlock block : hierarchy.get(key)) {
                    // if it is the first iteration put default control
                    if (totalT == 0) {
                        DenseDoubleMatrix1D simpleControl = new DenseDoubleMatrix1D(block.getCurrentL());
                        simpleControl.assign(1);
                        block.initialization(simpleControl);
                    }
                    // for high levels of hierarchy
                    if (block.getChildBlocks().size() > 0) {
                        // calculate length of input vector
                        int length = 0;
                        for (RecognitionBlock child : block.getChildBlocks()) {
                            length += child.getCurrentL();
                        }
                        DoubleMatrix1D input = new SparseDoubleMatrix1D(length);
                        // put all inputs in one vector
                        int currentIndex = 0;
                        for (RecognitionBlock child : block.getChildBlocks()) {
                            input.viewPart(currentIndex, child.getCurrentL()).assign(child.getCurrentOutput());
                            currentIndex++;
                        }
                        // calculate output and child control
                        DoubleMatrix1D control = block.iterate(input);
                        currentIndex = 0;
                        // collect control for children
                        for (RecognitionBlock child : block.getChildBlocks()) {
                            DoubleMatrix1D prevControl = controlVectors.get(child);
                            if (prevControl == null) {
                                controlVectors.put(child, control.viewPart(currentIndex, child.getCurrentL()).copy());
                            } else {
                                controlVectors.put(child, prevControl.assign(control.viewPart(currentIndex, child.getCurrentL()), DoublePlusMultFirst.minusMult(1)));
                            }
                        }
                    } else {
                        block.iterate(inputForFirstLevel[counter]);
                        counter++;
                    }
                }
                // init each child with control vector
                for (RecognitionBlock block : controlVectors.keySet()) {
                    DoubleMatrix1D control = controlVectors.get(block);
                    double maxValue = control.getMaxLocation()[0];
                    control.assign(DoubleMult.div(maxValue));
                    block.initialization(control);
                }
            }
        }
        totalT++;
    }
}
