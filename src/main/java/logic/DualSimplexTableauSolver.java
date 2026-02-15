package logic;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.fraction.Fraction;

public class DualSimplexTableauSolver {
    private final Tableau matrix;
    private final List<Integer> baseVariablesIndices;
    private final int objectiveRow;


    public DualSimplexTableauSolver(Tableau matrix, List<Integer> baseVariablesIndices) {
        this.matrix = new Tableau(matrix);
        this.baseVariablesIndices = new ArrayList<>(baseVariablesIndices);
        this.objectiveRow = baseVariablesIndices.size();
    }


    private boolean isSolved() {
        for (int i = 0; i < matrix.getColumnDimension() - 1; i++) {
            if (FractionUtils.isStrictlyNegative(matrix.getEntry(objectiveRow, i))) {
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

        for (int i = 0; i < this.matrix.getColumnDimension() - 1; i++) {

            if ((maxIndex < 0) || (matrix.getEntry(objectiveRow, i).compareTo(maxValue) >= 0)) {
                maxIndex = i;
                maxValue = matrix.getEntry(objectiveRow, i);
            }
        }

        return maxIndex;
    }    


}
