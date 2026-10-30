package logic;

import models.SimplexIterationResult;
import org.apache.commons.math3.fraction.Fraction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimplexTableauSolverTest {

    @Test
    void testAlreadyOptimalTableau() {
        // 1 constraint, 1 slack, 1 RHS
        // Objective row has all non-positive entries: [-2, 0, -10]
        Tableau tableau = new Tableau(2, 3);
        tableau.setEntry(0, 0, new Fraction(1));
        tableau.setEntry(0, 1, new Fraction(1));
        tableau.setEntry(0, 2, new Fraction(5));

        tableau.setEntry(1, 0, new Fraction(-2));
        tableau.setEntry(1, 1, new Fraction(0));
        tableau.setEntry(1, 2, new Fraction(-10));

        SimplexTableauSolver solver = new SimplexTableauSolver(tableau, List.of(1));
        SimplexIterationResult result = solver.solve();

        assertEquals(SimplexIterationResult.Status.OPTIMAL, result.status());
        assertNull(result.enteringVar());
        assertNull(result.leavingVar());
    }

    @Test
    void testSinglePivotStep() {
        // Constraint: x1 + s1 = 4
        // Objective: maximize 2x1 -> objective row has [2, 0, 0]
        Tableau tableau = new Tableau(2, 3);
        tableau.setEntry(0, 0, new Fraction(1));
        tableau.setEntry(0, 1, new Fraction(1));
        tableau.setEntry(0, 2, new Fraction(4));

        tableau.setEntry(1, 0, new Fraction(2));
        tableau.setEntry(1, 1, new Fraction(0));
        tableau.setEntry(1, 2, new Fraction(0));

        SimplexTableauSolver solver = new SimplexTableauSolver(tableau, List.of(1));
        SimplexIterationResult result = solver.solve();

        assertEquals(0, result.enteringVar());
        assertEquals(0, result.leavingVar());
    }

    @Test
    void testUnboundedDetection() {
        // Constraint: -x1 + s1 = 2 (pivot col 0 has negative entry -1)
        // Objective row has positive entry 3 for col 0
        Tableau tableau = new Tableau(2, 3);
        tableau.setEntry(0, 0, new Fraction(-1));
        tableau.setEntry(0, 1, new Fraction(1));
        tableau.setEntry(0, 2, new Fraction(2));

        tableau.setEntry(1, 0, new Fraction(3));
        tableau.setEntry(1, 1, new Fraction(0));
        tableau.setEntry(1, 2, new Fraction(0));

        SimplexTableauSolver solver = new SimplexTableauSolver(tableau, List.of(1));
        SimplexIterationResult result = solver.solve();

        assertEquals(SimplexIterationResult.Status.UNBOUNDED, result.status());
    }
}
