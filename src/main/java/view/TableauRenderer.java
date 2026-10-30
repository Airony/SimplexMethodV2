package view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.linear.FieldMatrix;

/**
 * Utility class for rendering Simplex tableaux as JavaFX GridPanes.
 */
public class TableauRenderer {

    /**
     * Creates a styled GridPane displaying a simplex tableau.
     *
     * @param tableau The tableau matrix to display
     * @param basisVariables Array of basis variable names (row headers)
     * @param columnHeaders Array of column header names (e.g., ["x1", "x2", "s1", "RHS"])
     * @param highlightRow Optional row to highlight (entering variable), -1 for none
     * @param highlightCol Optional column to highlight (leaving variable), -1 for none
     * @return A GridPane containing the formatted tableau
     */
    public static GridPane createTableauGrid(FieldMatrix<Fraction> tableau,
                                             String[] basisVariables,
                                             String[] columnHeaders,
                                             int highlightRow,
                                             int highlightCol) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);
        grid.setPadding(new Insets(8));
        grid.setStyle(
                "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;");

        int rows = tableau.getRowDimension();
        int cols = tableau.getColumnDimension();

        // Top-left corner (empty)
        Label corner = new Label("Basis");
        corner.setStyle("-fx-font-weight: bold; -fx-padding: 4 8;");
        grid.add(corner, 0, 0);

        // Column headers
        for (int j = 0; j < columnHeaders.length; j++) {
            Label header = new Label(columnHeaders[j]);
            header.setStyle("-fx-font-weight: bold; -fx-padding: 4 8; -fx-alignment: center;");
            if (j == highlightCol) {
                header.setStyle("-fx-font-weight: bold; -fx-padding: 4 8; -fx-alignment: center; " +
                        "-fx-background-color: #ffe6e6; -fx-border-color: #ff4444; -fx-border-width: 2;");
            }
            grid.add(header, j + 1, 0);
        }
        if (columnHeaders.length == cols - 1) {
            Label rhsHeader = new Label("RHS");
            rhsHeader.setStyle("-fx-font-weight: bold; -fx-padding: 4 8; -fx-alignment: center;");
            grid.add(rhsHeader, cols, 0);
        }

        // Tableau rows
        for (int i = 0; i < rows; i++) {
            // Row header (basis variable)
            Label rowHeader = new Label(basisVariables != null && i < basisVariables.length ?
                    basisVariables[i] : "Row " + i);
            rowHeader.setStyle("-fx-font-weight: bold; -fx-padding: 4 8;");
            if (i == highlightRow) {
                rowHeader.setStyle("-fx-font-weight: bold; -fx-padding: 4 8; " +
                        "-fx-background-color: #e6f3ff; -fx-border-color: #4444ff; -fx-border-width: 2;");
            }
            grid.add(rowHeader, 0, i + 1);

            // Data cells
            for (int j = 0; j < cols; j++) {
                Fraction value = tableau.getEntry(i, j);
                String displayValue = formatFraction(value);

                Label cell = new Label(displayValue);
                cell.setStyle("-fx-padding: 4 8; -fx-alignment: center-right; -fx-min-width: 60;");

                // Highlight pivot element
                if (i == highlightRow && j == highlightCol) {
                    cell.setStyle(
                            "-fx-padding: 4 8; -fx-alignment: center-right; -fx-min-width: 60; " +
                                    "-fx-background-color: #ffcccc; -fx-border-color: #ff0000; " +
                                    "-fx-border-width: 2; -fx-font-weight: bold;");
                }
                // Highlight entering column
                else if (j == highlightCol) {
                    cell.setStyle(
                            "-fx-padding: 4 8; -fx-alignment: center-right; -fx-min-width: 60; " +
                                    "-fx-background-color: #ffe6e6;");
                }
                // Highlight leaving row
                else if (i == highlightRow) {
                    cell.setStyle(
                            "-fx-padding: 4 8; -fx-alignment: center-right; -fx-min-width: 60; " +
                                    "-fx-background-color: #e6f3ff;");
                }

                grid.add(cell, j + 1, i + 1);
            }
        }

        return grid;
    }

    /**
     * Simplified version without highlighting.
     */
    public static GridPane createTableauGrid(FieldMatrix<Fraction> tableau,
                                             String[] basisVariables,
                                             String[] columnHeaders) {
        return createTableauGrid(tableau, basisVariables, columnHeaders, -1, -1);
    }

    /**
     * Formats a Fraction for display.
     * Shows as integer if denominator is 1, otherwise as fraction or decimal.
     */
    private static String formatFraction(Fraction f) {
        if (f.getDenominator() == 1) {
            return String.valueOf(f.getNumerator());
        } else if (Math.abs(f.getNumerator()) > 1000 || Math.abs(f.getDenominator()) > 1000) {
            // For large numbers, show decimal
            return String.format("%.3f", f.doubleValue());
        } else {
            return f.getNumerator() + "/" + f.getDenominator();
        }
    }

    /**
     * Creates column headers for a standard simplex tableau.
     *
     * @param numOriginalVars Number of original decision variables
     * @param numSlackVars Number of slack variables
     * @param numArtificialVars Number of artificial variables
     * @param includeRHS Whether to include RHS column header
     * @return Array of column header strings
     */
    public static String[] createStandardHeaders(int numOriginalVars,
                                                 int numSlackVars,
                                                 int numArtificialVars,
                                                 boolean includeRHS) {
        int totalCols = numOriginalVars + numSlackVars + numArtificialVars + (includeRHS ? 1 : 0);
        String[] headers = new String[totalCols];

        int idx = 0;

        // Original variables
        for (int i = 0; i < numOriginalVars; i++) {
            headers[idx++] = "x" + (i + 1);
        }

        // Slack variables
        for (int i = 0; i < numSlackVars; i++) {
            headers[idx++] = "s" + (i + 1);
        }

        // Artificial variables
        for (int i = 0; i < numArtificialVars; i++) {
            headers[idx++] = "a" + (i + 1);
        }

        // RHS
        if (includeRHS) {
            headers[idx] = "RHS";
        }

        return headers;
    }
}