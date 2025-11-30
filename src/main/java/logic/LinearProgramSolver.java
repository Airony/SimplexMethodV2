package logic;

import models.Constraint;
import models.ConstraintType;
import models.LPProblem;
import models.LinearProgramSolverOutput;
import models.SimplexIterationResult;
import org.apache.commons.math3.fraction.Fraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class LinearProgramSolver {
    private final Map<Integer, Double> slackVariablesCoefficients = new HashMap<>();
    private final HashSet<Integer> artificialVariablesCoefficients = new HashSet<>();
    private final Tableau tableau;
    private final List<Integer> basisIndices;
    private final List<String> variableNames = new ArrayList<>();
    private final SimplexStepListener simplexStepListener;
    private final boolean isMax;

    private final int numVariables;
    private final int numNormalVariables;
    private final int numSlackVariables;
    private final int numArtificialVariables;

    public LinearProgramSolver(LPProblem lpProblem, SimplexStepListener simplexStepListener) {
        isMax = lpProblem.getObjectiveType() == LPProblem.ObjectiveType.MAXIMIZE;
        this.simplexStepListener = simplexStepListener;


        List<Equation> equations = processConstraints(lpProblem.getConstraints());

        numNormalVariables = lpProblem.getObjectiveCoefficients().length;
        numSlackVariables = slackVariablesCoefficients.size();
        numArtificialVariables = artificialVariablesCoefficients.size();
        numVariables = numNormalVariables + numSlackVariables + numArtificialVariables;
        int numRows = calculateNumRows(lpProblem.getConstraints().size());

        tableau = initializeTableau(numRows, numVariables + 1);

        fillConstraintRows(equations, lpProblem.getConstraints().size(), numVariables);
        fillObjectiveFunctionRow(numRows, lpProblem.getObjectiveCoefficients());

        if (!artificialVariablesCoefficients.isEmpty()) {
            buildArtificialObjectiveFunctionRow(
                    lpProblem.getConstraints().size(),
                    numRows,
                    numVariables);
        }
        buildVariableNames();

        System.out.println("Initial Simplex Tableau:");
        tableau.print();

        basisIndices = initializeBasisIndices();
    }

    public void buildVariableNames() {
        variableNames.clear();
        for (int i = 0; i < numNormalVariables; i++) {
            variableNames.add("x" + (i + 1));
        }
        for (int i = 0; i < numSlackVariables; i++) {
            variableNames.add("s" + (i + 1));
        }
        for (int i = 0; i < numArtificialVariables; i++) {
            variableNames.add("a" + (i + 1));
        }
    }

    public void solve() {

        Tableau phase2Tableau = tableau;
        List<Integer> phase2BasisIndices = basisIndices;

        if (!artificialVariablesCoefficients.isEmpty()) {
            System.out.println("Starting Phase 1 Simplex Solver:");
            var phase1Result = solvePhase1();
            if (phase1Result == null) {
                return;
            }
            phase2Tableau = preparePhase2Tableau(phase1Result.tableau());
            phase2BasisIndices = phase1Result.baseVariablesIndices();
        }

        SimplexIterationResult phase2Result = solvePhase2(phase2Tableau, phase2BasisIndices);

        var extractedSolution = extractSolution(
                phase2Result.tableau(),
                phase2Result.baseVariablesIndices());

        simplexStepListener.onPhase2Complete(
                extractedSolution.optimalValue(),
                extractedSolution.variableValues().toArray(new Fraction[0]),
                ConstraintType.SolutionStatus.OPTIMAL);
    }

    private SimplexIterationResult solvePhase2(Tableau phase2Tableau,
                                               List<Integer> phase2BasisIndices

    ) {
        // Need to exclude the aritifical variables from the variable names
        // Basically exclude the last 'artificialVariablesCoefficients.size()' variable names
        var phase2VariableNames = variableNames.subList(
                0,
                numVariables - numArtificialVariables);

        var phase2Solver = new SimplexTableauSolver(phase2Tableau, phase2BasisIndices);
        System.out.println("Starting Phase 2 Simplex Solver:");
        SimplexIterationResult phase2Result;
        Tableau previousTableau = new Tableau(phase2Tableau);

        int iteration = 1;
        do {
            phase2Result = phase2Solver.solve();
            simplexStepListener.onPhase2Step(
                    previousTableau,
                    phase2Result.enteringVar() != null ? variableNames.get(phase2Result.enteringVar()) : null,
                    phase2Result.leavingVar() != null ? variableNames.get(phase2Result.leavingVar()) : null,
                    iteration,
                    phase2Result.baseVariablesIndices().stream().map(variableNames::get).toList(),
                    phase2VariableNames);
            previousTableau = new Tableau(phase2Result.tableau());
            iteration++;
        } while (phase2Result.status() == SimplexIterationResult.Status.CONTINUE);
        if (phase2Result.status() == SimplexIterationResult.Status.UNBOUNDED) {
            simplexStepListener.onPhase2Complete(
                    null,
                    null,
                    ConstraintType.SolutionStatus.UNBOUNDED);
        }
        return phase2Result;
    }

    /**
     *
     * @return SimplexIterationResult of phase 1 if the problem is feasible (final objective is
     * zero), null otherwise
     **/
    private SimplexIterationResult solvePhase1() {
        Tableau previousTableau = new Tableau(tableau);
        SimplexTableauSolver simplexTableauSolver = new SimplexTableauSolver(tableau, basisIndices);
        SimplexIterationResult phase1Result;
        int iteration = 1;
        do {
            phase1Result = simplexTableauSolver.solve();
            simplexStepListener.onPhase1Step(
                    previousTableau,
                    phase1Result.enteringVar() != null ? variableNames.get(phase1Result.enteringVar()) : null,
                    phase1Result.leavingVar() != null ? variableNames.get(phase1Result.leavingVar()) : null,
                    iteration,
                    phase1Result.baseVariablesIndices()
                            .stream()
                            .map(variableNames::get)
                            .toList(),
                    variableNames);
            iteration++;

            previousTableau = new Tableau(phase1Result.tableau());
        } while (phase1Result.status() == SimplexIterationResult.Status.CONTINUE);

        if (phase1Result.status() != SimplexIterationResult.Status.OPTIMAL) {
            simplexStepListener.onPhase1Complete(false);
            return null;
        }

        Fraction objectiveValue = phase1Result.tableau()
                .getEntry(
                        phase1Result.tableau().getRowDimension() - 2,
                        phase1Result.tableau().getColumnDimension() - 1);

        if (FractionUtils.isZero(objectiveValue)) {
            simplexStepListener.onPhase1Complete(true);
            return phase1Result;
        } else {
            simplexStepListener.onPhase1Complete(false);
            return null;
        }
    }


    private List<Equation> processConstraints(List<Constraint> constraints) {
        List<Equation> equations = new ArrayList<>();
        int k = -1;

        for (Constraint constraint : constraints) {
            k++;
            // For an equality constraint, we need to add an artificial variable
            if (constraint.getType() == ConstraintType.EQUAL) {
                equations.add(new Equation(constraint.getCoefficients(), constraint.getValue()));
                artificialVariablesCoefficients.add(k);
                continue;
            }

            // For the less than or equal constraints,
            // We check if the RHS is negative,
            // if so we get a1x1 + ... + anxn + s1 = -b with b positive
            // We multiply by -1 so we get -a1x1 + ... + -anxn -s1 = b with b positive
            // So the slack is -1.0, we will also add an artifical  variable
            // For greater than or equal constraints, transform to less than or equal and continue
            //
            if (constraint.getType() == ConstraintType.GREATER_THAN_OR_EQUAL) {
                constraint.transformToLessThanOrEqual();
            }
            if (constraint.getValue() < 0) {
                artificialVariablesCoefficients.add(k);
                slackVariablesCoefficients.put(k, -1.0);
                equations.add(new Equation(
                        scaleCoefficients(constraint.getCoefficients()),
                        -1 * constraint.getValue()));
            } else {
                equations.add(new Equation(constraint.getCoefficients(), constraint.getValue()));
                slackVariablesCoefficients.put(k, 1.0);
            }
        }
        return equations;
    }


    private int calculateNumRows(int numConstraints) {
        return numConstraints + 1 + (!artificialVariablesCoefficients.isEmpty() ? 1 : 0);
    }

    /**
     * Initializes the simplex tableau and fill it with zeroes.
     **/
    private Tableau initializeTableau(int numRows, int numCols) {
        Tableau tableau = new Tableau(numRows, numCols);
        tableau.fillZeroes();
        return tableau;
    }

    /**
     * Fills the constraint rows of the simplex tableau.
     **/
    private void fillConstraintRows(List<Equation> equations,
                                    int numConstraints,
                                    int numVariables) {
        for (int i = 0; i < numConstraints; i++) {
            // Put coefficients
            Equation equation = equations.get(i);
            for (int j = 0; j < equation.coefficients.size(); j++) {
                tableau.setEntry(i, j, new Fraction(equation.coefficients.get(j)));
            }
            // Put RHS
            tableau.setEntry(i, numVariables, new Fraction(equation.value()));
        }

        // Place slack variables
        int slackVariablesStartIndex = numNormalVariables;
        for (var entry : slackVariablesCoefficients.entrySet()) {
            int rowIndex = entry.getKey();
            double coef = entry.getValue();
            tableau.setEntry(rowIndex, slackVariablesStartIndex, new Fraction(coef));
            slackVariablesStartIndex++;
        }

        // Place artificial variables
        int artificialVariablesStartIndex = slackVariablesStartIndex;
        for (var entry : artificialVariablesCoefficients) {
            tableau.setEntry(entry, artificialVariablesStartIndex, new Fraction(1));
            artificialVariablesStartIndex++;
        }

    }

    private void fillObjectiveFunctionRow(int numRows, double[] objectiveFunctionCoefficients) {

        List<Double> finalCoefficients =
                new ArrayList<>(Arrays.stream(objectiveFunctionCoefficients).boxed().toList());
        if (!isMax) {
            finalCoefficients = scaleCoefficients(finalCoefficients);
        }
        finalCoefficients.addAll(new ArrayList<>(numSlackVariables + numArtificialVariables));

        for (int i = 0; i < finalCoefficients.size(); i++) {
            tableau.setEntry(numRows - 1, i, new Fraction(finalCoefficients.get(i)));
        }
    }

    private void buildArtificialObjectiveFunctionRow(int numConstraints,
                                                     int numRows,
                                                     int numVariables) {
        for (int i = 0; i < numNormalVariables + numSlackVariables; i++) {
            sumArtificialVariableRows(numConstraints, numRows, i);
        }

        sumArtificialVariableRows(numConstraints, numRows, numVariables);
    }

    /**
     * Calculates the sum of the artificial variable rows for a given column index.
     * And put it at the artificial objective function row. (second last row)

     */
    private void sumArtificialVariableRows(int numConstraints, int numRows, int colIndex) {
        Fraction rhsSum = Fraction.ZERO;
        for (int j = 0; j < numConstraints; j++) {
            if (!artificialVariablesCoefficients.contains(j)) {
                continue;
            }
            rhsSum = rhsSum.add(tableau.getEntry(j, colIndex));
        }
        tableau.setEntry(numRows - 2, colIndex, rhsSum);
    }

    private Tableau preparePhase2Tableau(Tableau phase1Tableau) {
        return phase1Tableau.removeRowsAndColumns(
                List.of(phase1Tableau.getRowDimension() - 2),
                getArtificialVariableColumnsIndices());
    }

    private List<Integer> getArtificialVariableColumnsIndices() {
        int last = tableau.getColumnDimension() - 2;
        int first = last - numArtificialVariables + 1;
        return IntStream.rangeClosed(first, last).boxed().toList();
    }

    /**
     * Extracts the final solution from the simplex tableau and basis indices.
     *
     * @param tableau      The final simplex tableau after solving.
     * @param basisIndices The indices of the basic variables.
     * @return
     */
    private LinearProgramSolverOutput extractSolution(Tableau tableau, List<Integer> basisIndices) {
        Fraction objectiveFunctionValue = tableau.getEntry(
                tableau.getRowDimension() - 1,
                tableau.getColumnDimension() - 1);
        if (!isMax) {
            objectiveFunctionValue = objectiveFunctionValue.multiply((-1));
        }
        System.out.println("Optimal objective function value: " + objectiveFunctionValue);

        List<Fraction> finalValues = new ArrayList<>();
        for (int i = 0; i < numNormalVariables; i++) {
            if (!basisIndices.contains(i)) {
                finalValues.add(Fraction.ZERO);
            } else {
                int rowIndex = basisIndices.indexOf(i);
                finalValues.add(tableau.getEntry(rowIndex, tableau.getColumnDimension() - 1));
            }
        }
        return new LinearProgramSolverOutput(finalValues, objectiveFunctionValue);
    }


    private List<Double> scaleCoefficients(List<Double> coefficients) {
        return new ArrayList<>(coefficients.stream().map(coef -> coef * -1.0).toList());
    }

    /**
     * Initializes the basis indices for the simplex tableau solver,
     * For a two-phase problem, takes the positive slack variables and all artificial variables.
     * For a single-phase problem, takes all the slack variables. (which will be all positive,
     * duh!:P )
     *
     */
    private List<Integer> initializeBasisIndices() {
        int firstSlackVariableIndex = numNormalVariables;

        List<Integer> indices = new ArrayList<>(slackVariablesCoefficients.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> entry.getKey() + firstSlackVariableIndex)
                .toList());

        if (!artificialVariablesCoefficients.isEmpty()) {
            indices.addAll(getArtificialVariableColumnsIndices());
        }
        indices.sort(Integer::compareTo);
        return indices;
    }

    //TODO: Do we really need this class?
    private record Equation(List<Double> coefficients, double value) {
    }

}
