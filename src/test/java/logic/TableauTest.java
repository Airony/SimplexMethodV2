package logic;

import org.apache.commons.math3.fraction.Fraction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableauTest {

    @Test
    void testDimensionsAndInitialization() {
        Tableau tableau = new Tableau(3, 4);
        assertEquals(3, tableau.getRowDimension());
        assertEquals(4, tableau.getColumnDimension());

        tableau.fillZeroes();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 4; c++) {
                assertEquals(Fraction.ZERO, tableau.getEntry(r, c));
            }
        }
    }

    @Test
    void testCopyConstructor() {
        Tableau original = new Tableau(2, 2);
        original.setEntry(0, 0, new Fraction(1));
        original.setEntry(0, 1, new Fraction(2));
        original.setEntry(1, 0, new Fraction(3));
        original.setEntry(1, 1, new Fraction(4));

        Tableau copy = new Tableau(original);
        assertEquals(original.getEntry(0, 0), copy.getEntry(0, 0));
        assertEquals(original.getEntry(1, 1), copy.getEntry(1, 1));

        // Ensure deep copy
        copy.setEntry(0, 0, new Fraction(99));
        assertEquals(new Fraction(1), original.getEntry(0, 0));
        assertEquals(new Fraction(99), copy.getEntry(0, 0));
    }

    @Test
    void testDivideRow() {
        Tableau tableau = new Tableau(2, 3);
        tableau.setEntry(0, 0, new Fraction(2));
        tableau.setEntry(0, 1, new Fraction(4));
        tableau.setEntry(0, 2, new Fraction(6));

        tableau.divideRow(0, new Fraction(2));

        assertEquals(new Fraction(1), tableau.getEntry(0, 0));
        assertEquals(new Fraction(2), tableau.getEntry(0, 1));
        assertEquals(new Fraction(3), tableau.getEntry(0, 2));
    }

    @Test
    void testSubtractMultipleOfRow() {
        Tableau tableau = new Tableau(2, 2);
        tableau.setEntry(0, 0, new Fraction(1));
        tableau.setEntry(0, 1, new Fraction(2));
        tableau.setEntry(1, 0, new Fraction(3));
        tableau.setEntry(1, 1, new Fraction(7));

        // row1 = row1 - 3 * row0 => [3 - 3*1, 7 - 3*2] = [0, 1]
        tableau.subtractMultipleOfRow(1, 0, new Fraction(3));

        assertEquals(new Fraction(0), tableau.getEntry(1, 0));
        assertEquals(new Fraction(1), tableau.getEntry(1, 1));
    }

    @Test
    void testRemoveRowsAndColumns() {
        Tableau tableau = new Tableau(3, 4);
        tableau.fillZeroes();
        tableau.setEntry(0, 0, new Fraction(10));
        tableau.setEntry(0, 2, new Fraction(20));
        tableau.setEntry(2, 0, new Fraction(30));
        tableau.setEntry(2, 2, new Fraction(40));

        // Remove row 1 and columns 1, 3
        Tableau reduced = tableau.removeRowsAndColumns(List.of(1), List.of(1, 3));

        assertEquals(2, reduced.getRowDimension());
        assertEquals(2, reduced.getColumnDimension());
        assertEquals(new Fraction(10), reduced.getEntry(0, 0));
        assertEquals(new Fraction(20), reduced.getEntry(0, 1));
        assertEquals(new Fraction(30), reduced.getEntry(1, 0));
        assertEquals(new Fraction(40), reduced.getEntry(1, 1));
    }
}
