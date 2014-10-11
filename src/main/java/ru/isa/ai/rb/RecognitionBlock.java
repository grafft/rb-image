package ru.isa.ai.rb;

import cern.colt.function.tdouble.DoubleFunction;
import cern.colt.function.tdouble.DoubleProcedure;
import cern.colt.function.tint.IntProcedure;
import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tbit.BitMatrix;
import cern.colt.matrix.tbit.BitVector;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tdouble.DoubleMult;
import cern.jet.math.tdouble.DoublePlusMultFirst;

import java.util.*;

/**
 * Created by GraffT on 09.10.2014.
 */
public class RecognitionBlock {
    private int id;
    private int currentQ = 100;
    private int currentL = 10;
    private int currentH = 10;
    private double controlThreshold = 0.5;
    private double distanceThreshold = 0.5;
    private int t = 0;

    private Map<Integer, List<BitMatrix>> predictionMatrices;
    private Map<Integer, List<DoubleMatrix2D>> learningMatrices;
    private List<RecognitionBlock> childBlocks = new ArrayList<>();

    private DoubleMatrix1D currentOutput;
    private DoubleMatrix1D controlVector;
    private IntArrayList currentActiveFeatures;
    private Map<Integer, List<BitMatrix>> activePredictionMatrices;

    public RecognitionBlock(int id, int currentH, int currentQ, double multiplier) {
        this.id = id;
        this.currentH = currentH;
        this.currentQ = currentQ;
        this.currentL = (int) (currentQ * multiplier);
        predictionMatrices = new HashMap<>();
        for (int i = 0; i < currentL; i++) {
            List<DoubleMatrix2D> learning = new ArrayList<>();
            DoubleMatrix2D firstLearn = new DenseDoubleMatrix2D(currentQ, currentH);
            //firstLearn.assign()
            List<BitMatrix> prediction = new ArrayList<>();

            predictionMatrices.put(i, prediction);
        }
    }

    public void initialization(DoubleMatrix1D controlVector) {
        t = 0;
        this.controlVector = controlVector;
        DoubleMatrix1D trimmed = controlVector.copy();
        trimmed.assign(new DoubleProcedure() {

            @Override
            public boolean apply(double element) {
                return element < controlThreshold;
            }
        }, 0);
        currentActiveFeatures = new IntArrayList();
        trimmed.getNonZeros(currentActiveFeatures, null);
        activePredictionMatrices = new HashMap<>();
        for (Map.Entry<Integer, List<BitMatrix>> entry : predictionMatrices.entrySet()) {
            activePredictionMatrices.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
    }

    public DoubleMatrix1D iterate(final DoubleMatrix1D inputVector) {
        // delete not fitted matrices
        final IntArrayList featuresToDel = new IntArrayList();
        currentActiveFeatures.forEach(new IntProcedure() {
            @Override
            public boolean apply(int element) {
                Iterator<BitMatrix> iterator = activePredictionMatrices.get(element).iterator();
                while (iterator.hasNext()) {
                    BitMatrix matrix = iterator.next();
                    BitVector column = matrix.part(t, 0, 1, matrix.rows()).toBitVector();
                    final DoubleMatrix1D input = inputVector.copy();
                    double module = input.aggregate(DoublePlusMultFirst.plusMult(1), new DoubleFunction() {
                        @Override
                        public double apply(double argument) {
                            return argument > 0 ? argument : -argument;
                        }
                    });
                    column.forEachIndexFromToInState(0, column.size(), true, new IntProcedure() {
                        @Override
                        public boolean apply(int element) {
                            input.setQuick(element, input.getQuick(element) - 1);
                            return true;
                        }
                    });
                    double distance = input.aggregate(DoublePlusMultFirst.plusMult(1), new DoubleFunction() {
                        @Override
                        public double apply(double argument) {
                            return argument > 0 ? argument : -argument;
                        }
                    });
                    if (distance / (module + column.cardinality()) >= distanceThreshold)
                        iterator.remove();
                }
                if (activePredictionMatrices.get(element).size() == 0)
                    featuresToDel.add(element);
                return true;
            }
        });
        // remove all empty features
        currentActiveFeatures.removeAll(featuresToDel);
        // calculate output as weighted number of prediction matrices and control vector to child
        currentOutput = new SparseDoubleMatrix1D(currentL);
        final DoubleMatrix1D controlToChild = (t < currentH - 1) ? new SparseDoubleMatrix1D(currentQ) : null;
        currentActiveFeatures.forEach(new IntProcedure() {
            @Override
            public boolean apply(int element) {
                // count fitted matrices
                currentOutput.setQuick(element, activePredictionMatrices.get(element).size());
                // sum columns from fitted matrices
                if (controlToChild != null) {
                    final DoubleMatrix1D additionToControl = new SparseDoubleMatrix1D(currentQ);
                    for (BitMatrix matrix : activePredictionMatrices.get(element)) {
                        BitVector column = matrix.part(t + 1, 0, 1, matrix.rows()).toBitVector();
                        column.forEachIndexFromToInState(0, column.size(), true, new IntProcedure() {
                            @Override
                            public boolean apply(int element) {
                                additionToControl.setQuick(element, additionToControl.getQuick(element) + 1);
                                return true;
                            }
                        });
                    }
                    additionToControl.assign(DoubleMult.mult(controlVector.getQuick(element)));
                    controlToChild.assign(additionToControl, DoublePlusMultFirst.plusMult(1));
                }
                return true;
            }
        });
        // normalize vectors
        double maxOutputValue = currentOutput.getMaxLocation()[0];
        currentOutput.assign(DoubleMult.div(maxOutputValue));
        if (controlToChild != null) {
            double maxControlValue = controlToChild.getMaxLocation()[0];
            controlToChild.assign(DoubleMult.div(maxControlValue));
        }
        t++;
        return controlToChild;
    }

    public DoubleMatrix1D getCurrentOutput() {
        return currentOutput;
    }

    public int getCurrentQ() {
        return currentQ;
    }

    public int getCurrentL() {
        return currentL;
    }

    public int getCurrentH() {
        return currentH;
    }

    public int getId() {
        return id;
    }

    public void addChild(RecognitionBlock rb) {
        childBlocks.add(rb);
    }

    public List<RecognitionBlock> getChildBlocks() {
        return childBlocks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecognitionBlock that = (RecognitionBlock) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
