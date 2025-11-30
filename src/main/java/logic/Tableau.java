package logic;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionField;
import org.apache.commons.math3.linear.Array2DRowFieldMatrix;

import java.util.List;

public class Tableau extends Array2DRowFieldMatrix<Fraction> {
    public Tableau(Tableau tableau) {
        super(tableau.getField(), tableau.getRowDimension(), tableau.getColumnDimension());
        for (int i = 0; i < tableau.getRowDimension(); i++) {
            for (int j = 0; j < tableau.getColumnDimension(); j++) {
                this.setEntry(i, j, tableau.getEntry(i, j));
            }
        }
    }


    public Tableau(
            int rowDimension,
            int columnDimension) throws NotStrictlyPositiveException {
        super(FractionField.getInstance(), rowDimension, columnDimension);
    }


    public void fillZeroes() {
        for (int i = 0; i < this.getRowDimension(); i++) {
            for (int j = 0; j < this.getColumnDimension(); j++) {
                this.setEntry(i, j, new Fraction(0));
            }
        }
    }

    public void divideRow(int rowIndex, Fraction divisor) {
        for (int j = 0; j < this.getColumnDimension(); j++) {
            this.setEntry(rowIndex, j, this.getEntry(rowIndex, j).divide(divisor));
        }
    }

    public void subtractMultipleOfRow(int targetRowIndex, int sourceRowIndex, Fraction multiple) {
        for (int j = 0; j < this.getColumnDimension(); j++) {
            Fraction value = this.getEntry(targetRowIndex, j)
                    .subtract(this.getEntry(sourceRowIndex, j).multiply(multiple));
            this.setEntry(targetRowIndex, j, value);
        }
    }

    public void print() {
        int rows = this.getRowDimension();
        int cols = this.getColumnDimension();

        // Compute max width per column (based on string representation)
        int[] colWidths = new int[cols];
        for (int c = 0; c < cols; c++) {
            int max = 0;
            for (int r = 0; r < rows; r++) {
                String s = this.getEntry(r, c).toString();  // e.g. "3/4"
                max = Math.max(max, s.length());
            }
            colWidths[c] = max;
        }

        // Print
        for (int r = 0; r < rows; r++) {
            System.out.print("[ ");
            for (int c = 0; c < cols; c++) {
                String s = this.getEntry(r, c).toString();
                // Right-align within column
                System.out.print(" ".repeat(colWidths[c] - s.length()) + s);

                if (c < cols - 1) System.out.print("  "); // spacing between columns
            }
            System.out.println(" ]");
        }
    }

    // Add method to remove several rows and columns at once
    public Tableau removeRowsAndColumns(List<Integer> rowsToRemoveList,
                                        List<Integer> colsToRemoveList) {
        int newRowCount = this.getRowDimension() - rowsToRemoveList.size();
        int newColCount = this.getColumnDimension() - colsToRemoveList.size();
        Tableau reducedTableau = new Tableau(newRowCount, newColCount);
        System.out.println("Removing rows " + rowsToRemoveList + " and columns " + colsToRemoveList);

        int newRowIdx = 0;
        for (int i = 0; i < this.getRowDimension(); i++) {
            boolean skipRow = false;
            for (int r : rowsToRemoveList) {
                if (i == r) {
                    skipRow = true;
                    break;
                }
            }
            if (skipRow) continue;

            int newColIdx = 0;
            for (int j = 0; j < this.getColumnDimension(); j++) {
                boolean skipCol = false;
                for (int c : colsToRemoveList) {
                    if (j == c) {
                        skipCol = true;
                        break;
                    }
                }
                if (skipCol) continue;

                reducedTableau.setEntry(newRowIdx, newColIdx, this.getEntry(i, j));
                newColIdx++;
            }
            newRowIdx++;
        }

        return reducedTableau;
    }
}

