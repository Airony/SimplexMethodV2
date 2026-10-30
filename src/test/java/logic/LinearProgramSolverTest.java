package logic;

import models.Constraint;
import models.ConstraintType;
import models.LPProblem;
import org.apache.commons.math3.fraction.Fraction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinearProgramSolverTest {

    static class SolutionCollector implements SimplexStepListener {
        Fraction optimalValue;
        Fraction[] solution;
        ConstraintType.SolutionStatus status;
        boolean phase1Completed;
        boolean phase1Feasible;
        int phase1Steps = 0;
        int phase2Steps = 0;
        String errorMessage;

        @Override
        public void onPhase1Step(Tableau tableau, String entering, String leaving, int iteration,
                                 List<String> basisVariableNames, List<String> variableNames) {
            phase1Steps++;
        }

        @Override
        public void onPhase1Complete(boolean feasible) {
            phase1Completed = true;
            phase1Feasible = feasible;
        }

        @Override
        public void onPhase2Step(Tableau tableau, String entering, String leaving, int iteration,
                                 List<String> basisVariableNames, List<String> variableNames) {
            phase2Steps++;
        }

        @Override
        public void onPhase2Complete(Fraction optimalValue, Fraction[] solution,
                                     ConstraintType.SolutionStatus status) {
            this.optimalValue = optimalValue;
            this.solution = solution;
            this.status = status;
        }

        @Override
        public void onError(String message) {
            this.errorMessage = message;
        }
    }

    @Test
    void testSingleVariableMaximization() {
        // Maximize Z = 4x1
        // Subject to: 2x1 <= 10
        LPProblem problem = new LPProblem(
                new double[]{4.0},
                List.of(new Constraint(10.0, ConstraintType.LESS_THAN_OR_EQUAL, List.of(2.0))),
                LPProblem.ObjectiveType.MAXIMIZE
        );

        SolutionCollector collector = new SolutionCollector();
        LinearProgramSolver solver = new LinearProgramSolver(problem, collector);
        solver.solve();

        assertEquals(ConstraintType.SolutionStatus.OPTIMAL, collector.status);
        assertEquals(new Fraction(20), collector.optimalValue);
        assertNotNull(collector.solution);
        assertEquals(new Fraction(5), collector.solution[0]);
    }

    @Test
    void testStandardTwoVariableMaximization() {
        // Maximize Z = 3x1 + 2x2
        // Subject to:
        //   2x1 + x2 <= 18
        //   2x1 + 3x2 <= 42
        //   3x1 + x2 <= 24
        LPProblem problem = new LPProblem(
                new double[]{3.0, 2.0},
                List.of(
                        new Constraint(18.0, ConstraintType.LESS_THAN_OR_EQUAL, List.of(2.0, 1.0)),
                        new Constraint(42.0, ConstraintType.LESS_THAN_OR_EQUAL, List.of(2.0, 3.0)),
                        new Constraint(24.0, ConstraintType.LESS_THAN_OR_EQUAL, List.of(3.0, 1.0))
                ),
                LPProblem.ObjectiveType.MAXIMIZE
        );

        SolutionCollector collector = new SolutionCollector();
        LinearProgramSolver solver = new LinearProgramSolver(problem, collector);
        solver.solve();

        assertEquals(ConstraintType.SolutionStatus.OPTIMAL, collector.status);
        assertEquals(new Fraction(33), collector.optimalValue);
        assertNotNull(collector.solution);
        assertEquals(new Fraction(3), collector.solution[0]);
        assertEquals(new Fraction(12), collector.solution[1]);
    }

    @Test
    void testMinimizationProblem() {
        // Minimize Z = 2x1 + 3x2
        // Subject to:
        //   x1 + x2 >= 4
        //   x1 + 3x2 >= 6
        LPProblem problem = new LPProblem(
                new double[]{2.0, 3.0},
                List.of(
                        new Constraint(4.0, ConstraintType.GREATER_THAN_OR_EQUAL, List.of(1.0, 1.0)),
                        new Constraint(6.0, ConstraintType.GREATER_THAN_OR_EQUAL, List.of(1.0, 3.0))
                ),
                LPProblem.ObjectiveType.MINIMIZE
        );

        SolutionCollector collector = new SolutionCollector();
        LinearProgramSolver solver = new LinearProgramSolver(problem, collector);
        solver.solve();

        assertEquals(ConstraintType.SolutionStatus.OPTIMAL, collector.status);
        assertEquals(new Fraction(9), collector.optimalValue);
        assertNotNull(collector.solution);
        assertEquals(new Fraction(3), collector.solution[0]);
        assertEquals(new Fraction(1), collector.solution[1]);
    }

    @Test
    void testEqualityConstraintPhaseOne() {
        // Maximize Z = x1 + x2
        // Subject to:
        //   x1 + 2x2 = 4
        //   2x1 + x2 <= 7
        LPProblem problem = new LPProblem(
                new double[]{1.0, 1.0},
                List.of(
                        new Constraint(4.0, ConstraintType.EQUAL, List.of(1.0, 2.0)),
                        new Constraint(7.0, ConstraintType.LESS_THAN_OR_EQUAL, List.of(2.0, 1.0))
                ),
                LPProblem.ObjectiveType.MAXIMIZE
        );

        SolutionCollector collector = new SolutionCollector();
        LinearProgramSolver solver = new LinearProgramSolver(problem, collector);
        solver.solve();

        assertTrue(collector.phase1Completed);
        assertTrue(collector.phase1Feasible);
        assertEquals(ConstraintType.SolutionStatus.OPTIMAL, collector.status);
        assertEquals(new Fraction(4), collector.optimalValue);
        assertNotNull(collector.solution);
        assertEquals(new Fraction(4), collector.solution[0]);
        assertEquals(new Fraction(0), collector.solution[1]);
    }

    @Test
    void testUnboundedProblem() {
        // Maximize Z = 2x1 - x2
        // Subject to:
        //   -x1 + x2 <= 2
        LPProblem problem = new LPProblem(
                new double[]{2.0, -1.0},
                List.of(new Constraint(2.0, ConstraintType.LESS_THAN_OR_EQUAL, List.of(-1.0, 1.0))),
                LPProblem.ObjectiveType.MAXIMIZE
        );

        SolutionCollector collector = new SolutionCollector();
        LinearProgramSolver solver = new LinearProgramSolver(problem, collector);
        solver.solve();

        assertEquals(ConstraintType.SolutionStatus.UNBOUNDED, collector.status);
    }

    @Test
    void testInfeasibleProblem() {
        // Maximize Z = x1 + x2
        // Subject to:
        //   x1 + x2 <= 2
        //   x1 + x2 >= 5
        LPProblem problem = new LPProblem(
                new double[]{1.0, 1.0},
                List.of(
                        new Constraint(2.0, ConstraintType.LESS_THAN_OR_EQUAL, List.of(1.0, 1.0)),
                        new Constraint(5.0, ConstraintType.GREATER_THAN_OR_EQUAL, List.of(1.0, 1.0))
                ),
                LPProblem.ObjectiveType.MAXIMIZE
        );

        SolutionCollector collector = new SolutionCollector();
        LinearProgramSolver solver = new LinearProgramSolver(problem, collector);
        solver.solve();

        assertFalse(collector.phase1Feasible);
    }
}
