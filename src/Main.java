import logic.LinearProgramSolver;
import logic.Tableau;
import models.Constraint;
import models.ConstraintType;
import org.apache.commons.math3.fraction.Fraction;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Examle Usage of LinearProgramSolver
        // Max Z = -x -y
        // 2x + y <= 6
        Tableau tab = new Tableau(5, 7);
        // Please set the current values :
        // 2 1 1 0 0 0 6
        // 1 1 0 0 1 0 4
        // 1 3 0 -1 0 1 8
        // 2 4 0 -1 0 0 12
        // -1 -1 0 0 0 0 0
        tab.fillZeroes();

        tab.setEntry(0, 0, new Fraction(2));
        tab.setEntry(0, 1, new Fraction(1));
        tab.setEntry(0, 2, new Fraction(1));
        tab.setEntry(0, 6, new Fraction(6));
        tab.setEntry(1, 0, new Fraction(1));
        tab.setEntry(1, 1, new Fraction(1));
        tab.setEntry(1, 4, new Fraction(1));
        tab.setEntry(1, 6, new Fraction(4));
        tab.setEntry(2, 0, new Fraction(1));
        tab.setEntry(2, 1, new Fraction(3));
        tab.setEntry(2, 3, new Fraction(-1));
        tab.setEntry(2, 5, new Fraction(1));
        tab.setEntry(2, 6, new Fraction(8));
        tab.setEntry(3, 0, new Fraction(2));
        tab.setEntry(3, 1, new Fraction(4));
        tab.setEntry(3, 3, new Fraction(-1));
        tab.setEntry(3, 6, new Fraction(12));
        tab.setEntry(4, 0, new Fraction(-1));
        tab.setEntry(4, 1, new Fraction(-1));

//        new SimplexTableauSolver(tab, List.of(2, 5, 6)).solve();


        Constraint c1 = new Constraint(
                List.of(2.0, 1.0),
                6.0,
                models.ConstraintType.LESS_THAN_OR_EQUAL
        );
        Constraint c2 = new Constraint(
                List.of(1.0, 1.0),
                4.0,
                ConstraintType.EQUAL
        );
        Constraint c3 = new Constraint(
                List.of(1.0, 3.0),
                8.0,
                ConstraintType.GREATER_THAN_OR_EQUAL
        );
        LinearProgramSolver solver = new LinearProgramSolver(
                List.of(c1, c2, c3),
                List.of(-1.0, -1.0)
        );
        solver.solve();
    }
}