package logic;

import models.SimplexTableauSolverDTO;
import org.apache.commons.math3.fraction.Fraction;

import java.util.ArrayList;
import java.util.List;

public class SimplexTableauSolver {
    private final Tableau mat;
    private final List<Integer> baseVariablesIndices;
    private final int objectiveRow;

    public SimplexTableauSolver(Tableau mat, List<Integer> baseVariablesIndices) {
        this.mat = new Tableau(mat);
        this.baseVariablesIndices = new ArrayList<>(baseVariablesIndices);
        this.objectiveRow = baseVariablesIndices.size();
    }


    public SimplexTableauSolverDTO solve() {

        while (!isSolved()) {
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
        for (int i = 0; i < mat.getColumnDimension() - 1; i++) {
            if (FractionUtils.isStrictlyPositive(mat.getEntry(objectiveRow, i))) {
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
        Fraction maxValue = null;

        for (int i = 0; i < this.mat.getColumnDimension() - 1; i++) {

            if ((maxIndex < 0) || (mat.getEntry(objectiveRow, i).compareTo(maxValue) >= 0)) {
                maxIndex = i;
                maxValue = mat.getEntry(objectiveRow, i);
            }
        }

        return maxIndex;
    }

    private int getLeavingVariable(int pivotColumn) {
        Fraction minQuotient = new Fraction(-1);
        int minIndex = -1;

        for (int i = 0; i < objectiveRow; i++) {

            Fraction currentQuotient = calculateQuotient(i, pivotColumn);

            if (FractionUtils.isStrictlyNegative(currentQuotient)) { // indicates negative a_ij
                continue;
            }
            if (currentQuotient.compareTo(minQuotient) <= 0 || FractionUtils.isNegative(minQuotient)) {
                minIndex = i;
                minQuotient = currentQuotient;
            }
        }

        if (minIndex < 0) {
            throw new RuntimeException("Unbounded Problem");
        }
        return minIndex;
    }

    private Fraction calculateQuotient(int rowIndex, int pivotColumn) {
        Fraction a = this.mat.getEntry(rowIndex, this.mat.getColumnDimension() - 1);
        Fraction b = this.mat.getEntry(rowIndex, pivotColumn);

        if (b.getDenominator() == 0) {
            return new Fraction(-1);
        }
        return a.divide(b);
    }


    private void normalizePivot(int pivotColumnIndex, int pivotRowIndex) {
        int rows = mat.getRowDimension();

        Fraction pivotVal = mat.getEntry(pivotRowIndex, pivotColumnIndex);

        // 1. Normalize pivot row
        mat.divideRow(pivotRowIndex, pivotVal);

        // 2. Eliminate pivot column in other rows
        for (int i = 0; i < rows; i++) {
            if (i == pivotRowIndex) continue;
            Fraction factor = mat.getEntry(i, pivotColumnIndex);
            if (factor.getDenominator() == 0) continue;
            mat.subtractMultipleOfRow(i, pivotRowIndex, factor);
        }
    }

}
