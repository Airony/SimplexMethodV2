package models;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LPProblemTest {

    @Test
    void testObjectiveTypeFromString() {
        assertEquals(LPProblem.ObjectiveType.MAXIMIZE, LPProblem.ObjectiveType.fromString("MAX"));
        assertEquals(LPProblem.ObjectiveType.MINIMIZE, LPProblem.ObjectiveType.fromString("MIN"));
        assertThrows(IllegalArgumentException.class, () -> LPProblem.ObjectiveType.fromString("MAXIMIZE"));
        assertThrows(IllegalArgumentException.class, () -> LPProblem.ObjectiveType.fromString(""));
    }

    @Test
    void testProblemConstruction() {
        double[] obj = {3.0, 5.0};
        List<Constraint> constraints = List.of(
                new Constraint(4.0, ConstraintType.LESS_THAN_OR_EQUAL, List.of(1.0, 0.0))
        );
        LPProblem problem = new LPProblem(obj, constraints, LPProblem.ObjectiveType.MAXIMIZE);

        assertArrayEquals(new double[]{3.0, 5.0}, problem.getObjectiveCoefficients());
        assertEquals(1, problem.getConstraints().size());
        assertEquals(LPProblem.ObjectiveType.MAXIMIZE, problem.getObjectiveType());
    }
}
