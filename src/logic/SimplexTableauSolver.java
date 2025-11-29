package logic;

import models.SimplexTableauSolverDTO;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.List;

public class SimplexTableauSolver {
    private final SimpleMatrix mat;
    private final List<Integer> baseVariablesIndices;
    private final int objectiveRow;

    public SimplexTableauSolver(SimpleMatrix mat, List<Integer> baseVariablesIndices) {
        this.mat = mat.copy();
        this.baseVariablesIndices = new ArrayList<>(baseVariablesIndices);
        this.objectiveRow = baseVariablesIndices.size();
    }


    public SimplexTableauSolverDTO solve() {
        int iters = 0;

        while (!isSolved() && iters <= 40) {
            iters++;
            this.mat.print();
            System.out.println("Basic variable indices " + baseVariablesIndices);
            int pivotCol = getPivotColumn();
            System.out.println("Pivot column is " + pivotCol);
            int replaceRowIndex = getLeavingVariable(pivotCol);
            System.out.println("Leaving variable " + replaceRowIndex);

            this.baseVariablesIndices.remove(replaceRowIndex); // Cast to Integer to remove by value
            this.baseVariablesIndices.add(pivotCol);
            normalizePivot(pivotCol, replaceRowIndex);
        }

        this.mat.print();
        System.out.println("Basic variable indices " + baseVariablesIndices);

        // Now what we need to do is extract the solution
        // We have the list of basic variables
        System.out.println("Basic variacles are " + baseVariablesIndices.toString());

        return new SimplexTableauSolverDTO(mat, baseVariablesIndices);

    }


    private boolean isSolved() {
        for (int i = 0; i < mat.getNumCols() - 1; i++) {
            if (mat.get(objectiveRow, i) > 0) {
                return false;
            }
        }
        return true;
    }

    /*
    Returns the index of the pivot column, or -1 if not found
     */
    private int getPivotColumn() {
        int maxIndex = -1;
        double maxValue = -1;

        for (int i = 0; i < this.mat.getNumCols() - 1; i++) {

            if (mat.get(objectiveRow, i) > maxValue) {
                maxIndex = i;
                maxValue = mat.get(objectiveRow, i);
            }
        }

        return maxIndex;
    }

    private int getLeavingVariable(int pivotColumn) {
        double minQuotient = -1;
        int minIndex = -1;

        for (int i = 0; i < objectiveRow; i++) {

            double currentQuotient = calculateQuotient(i, pivotColumn);


            if (currentQuotient < 0) { // indicates negative a_ij
                continue;
            }
            if (currentQuotient < minQuotient || minQuotient <= 0) {
                minIndex = i;
                minQuotient = currentQuotient;
            }
        }

        if (minIndex < 0) {
            throw new RuntimeException("Unbounded Problem");
        }
        return minIndex;
    }

    private double calculateQuotient(int rowIndex, int pivotColumn) {
        double a = this.mat.get(rowIndex, this.mat.getNumCols() - 1);
        double b = this.mat.get(rowIndex, pivotColumn);

        System.out.println("Calculating quotient for row " + rowIndex + ": " + a + " / " + b + " = " + (b == 0 ? "undefined" : (a / b)));
        if (b == 0) {
            return -1;
        }
        return a / b;
    }


    private void normalizePivot(int pivotColumnIndex, int pivotRowIndex) {
        int rows = mat.getNumRows();
        int cols = mat.getNumCols();

        double pivotVal = mat.get(pivotRowIndex, pivotColumnIndex);

        // 1. Normalize pivot row
        for (int j = 0; j < cols; j++) {
            mat.set(pivotRowIndex, j, mat.get(pivotRowIndex, j) / pivotVal);
        }

        // 2. Eliminate pivot column in other rows
        for (int i = 0; i < rows; i++) {
            if (i == pivotRowIndex) continue;

            double factor = mat.get(i, pivotColumnIndex);
            if (factor == 0) continue;

            for (int j = 0; j < cols; j++) {
                double newVal = mat.get(i, j) - factor * mat.get(pivotRowIndex, j);
                mat.set(i, j, newVal);
            }
        }
    }

}

