package logic;

import models.ConstraintType;
import org.apache.commons.math3.fraction.Fraction;

import java.util.List;

/**
 * Listener interface for receiving step-by-step updates from the Simplex solver.
 * Implement this interface to receive callbacks after each pivot operation.
 */
public interface SimplexStepListener {

    /**
     * Called after each pivot operation in Phase 1 (finding initial feasible solution).
     *
     * @param tableau The current simplex tableau after the pivot
     * @param entering The entering variable name (e.g., "x1", "x2")
     * @param leaving The leaving variable name (e.g., "s1", "a1")
     * @param iteration The iteration number (1-indexed)
     */
    void onPhase1Step(Tableau tableau,
                      String entering,
                      String leaving,
                      int iteration,
                      List<String> basisVariableNames,
                      List<String> variableNames);

    /**
     * Called when Phase 1 is complete.
     *
     * @param feasible True if a feasible solution was found, false otherwise
     */
    void onPhase1Complete(boolean feasible);

    /**
     * Called after each pivot operation in Phase 2 (optimizing objective function).
     *
     * @param tableau The current simplex tableau after the pivot
     * @param entering The entering variable name
     * @param leaving The leaving variable name
     * @param iteration The iteration number (1-indexed)
     */
    void onPhase2Step(Tableau tableau,
                      String entering,
                      String leaving,
                      int iteration,
                      List<String> basisVariableNames,
                      List<String> variableNames);

    /**
     * Called when Phase 2 is complete with the final solution.
     *
     * @param optimalValue The optimal objective function value
     * @param solution     Array of variable values in order [x1, x2, ..., xn]
     * @param status       The solution status (OPTIMAL, UNBOUNDED, INFEASIBLE)
     */
    void onPhase2Complete(Fraction optimalValue,
                          Fraction[] solution, ConstraintType.SolutionStatus status);

    /**
     * Called if an error occurs during solving.
     *
     * @param message The error message
     */
    void onError(String message);
}