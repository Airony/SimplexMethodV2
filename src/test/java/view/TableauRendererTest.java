package view;

import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import logic.Tableau;
import org.apache.commons.math3.fraction.Fraction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableauRendererTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Platform already started
        }
    }

    @Test
    void testCreateTableauGrid() {
        Tableau tableau = new Tableau(2, 3);
        tableau.setEntry(0, 0, new Fraction(1));
        tableau.setEntry(0, 1, new Fraction(2));
        tableau.setEntry(0, 2, new Fraction(3));
        tableau.setEntry(1, 0, new Fraction(4));
        tableau.setEntry(1, 1, new Fraction(5));
        tableau.setEntry(1, 2, new Fraction(6));

        String[] basis = {"s1", "s2"};
        String[] headers = {"x1", "x2", "RHS"};

        GridPane grid = TableauRenderer.createTableauGrid(tableau, basis, headers, 0, 1);
        assertNotNull(grid);
        assertFalse(grid.getChildren().isEmpty());
    }

    @Test
    void testCreateTableauGridAppendsMissingRHSHeader() {
        Tableau tableau = new Tableau(2, 3);
        tableau.setEntry(0, 0, new Fraction(1));
        tableau.setEntry(0, 1, new Fraction(2));
        tableau.setEntry(0, 2, new Fraction(3));
        tableau.setEntry(1, 0, new Fraction(4));
        tableau.setEntry(1, 1, new Fraction(5));
        tableau.setEntry(1, 2, new Fraction(6));

        String[] basis = {"s1", "s2"};
        String[] headers = {"x1", "x2"};

        GridPane grid = TableauRenderer.createTableauGrid(tableau, basis, headers);
        assertNotNull(grid);
        assertFalse(grid.getChildren().isEmpty());
    }

    @Test
    void testCreateStandardHeadersWithRHS() {
        String[] headers = TableauRenderer.createStandardHeaders(2, 2, 1, true);
        assertArrayEquals(new String[]{"x1", "x2", "s1", "s2", "a1", "RHS"}, headers);
    }

    @Test
    void testCreateStandardHeadersWithoutRHS() {
        String[] headers = TableauRenderer.createStandardHeaders(3, 1, 0, false);
        assertArrayEquals(new String[]{"x1", "x2", "x3", "s1"}, headers);
    }
}

