import logic.LinearProgramSolver;
import models.Constraint;
import models.ConstraintType;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Examle Usage of LinearProgramSolver
        // Max Z = -x -y
        // 2x + y <= 6

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
    }
}