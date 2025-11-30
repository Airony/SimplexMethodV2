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
import java.util.List;
import java.util.Map;

public class LinearProgramSolver {
    private final List<Double> maxObjectiveFunctionCoefficients;
    private final Map<Integer, Double> slackVariablesCoefficients = new HashMap<>();
    private final Map<Integer, Double> artificialVariablesCoefficients = new HashMap<>();
    private final Tableau tableau;
    private final List<Integer> basisIndices;
    private final List<String> variableNames = new ArrayList<>();
    private final SimplexStepListener simplexStepListener;
    private boolean isMax;

    public LinearProgramSolver(LPProblem lpProblem, SimplexStepListener simplexStepListener) {
        this.maxObjectiveFunctionCoefficients = Arrays.stream(lpProblem.getObjectiveCoefficients())
                .boxed()
                .toList();
        isMax = true;
        if (lpProblem.getObjectiveType() == LPProblem.ObjectiveType.MINIMIZE) {
            isMax = false;
            scaleCoefficients(this.maxObjectiveFunctionCoefficients, -1.0);
        }
        this.simplexStepListener = simplexStepListener;


        List<Equation> equations = processConstraints(lpProblem.getConstraints());

        int numVariables = calculateNumVariables();
        int numRows = calculateNumRows(lpProblem.getConstraints().size());

        tableau = initializeTableau(numRows, numVariables + 1);

        fillConstraintRows(equations, lpProblem.getConstraints().size(), numVariables);
        fillObjectiveFunctionRow(numRows);

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

    public List<String> buildVariableNames() {
        variableNames.clear();
        for (int i = 0; i < maxObjectiveFunctionCoefficients.size(); i++) {
            variableNames.add("x" + (i + 1));
        }
        for (int i = 0; i < slackVariablesCoefficients.size(); i++) {
            variableNames.add("s" + (i + 1));
        }
        for (int i = 0; i < artificialVariablesCoefficients.size(); i++) {
            variableNames.add("a" + (i + 1));
        }
        return variableNames;
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
        // Need to exclude the aritifical variables from the variable names
        // Basically exclude the last 'artificialVariablesCoefficients.size()' variable names
        var phase2VariableNames = variableNames.subList(
                0,
                variableNames.size() - artificialVariablesCoefficients.size());


        var phase2Solver = new SimplexTableauSolver(phase2Tableau, phase2BasisIndices);
        System.out.println("Starting Phase 2 Simplex Solver:");
        SimplexIterationResult phase2Result;
        int iteration = 1;
        do {
            phase2Result = phase2Solver.solve();
            simplexStepListener.onPhase2Step(
                    phase2Result.tableau(),
                    phase2Result.enteringVar() != null ? variableNames.get(phase2Result.enteringVar()) : null,
                    phase2Result.leavingVar() != null ? variableNames.get(phase2Result.leavingVar()) : null,
                    iteration,
                    phase2Result.baseVariablesIndices()
                            .stream()
                            .map(variableNames::get)
                            .toList(),
                    phase2VariableNames);
            iteration++;
        } while (phase2Result.status() == SimplexIterationResult.Status.CONTINUE);
        if (phase2Result.status() == SimplexIterationResult.Status.UNBOUNDED) {
            simplexStepListener.onPhase2Complete(phase2Result.tableau(),
                    null, null, ConstraintType.SolutionStatus.UNBOUNDED);
        }

        var extractedSolution = extractSolution(
                phase2Result.tableau(),
                phase2Result.baseVariablesIndices());

        simplexStepListener.onPhase2Complete(
                phase2Result.tableau(),
                extractedSolution.optimalValue(),
                extractedSolution.variableValues().toArray(new Fraction[0]),
                ConstraintType.SolutionStatus.OPTIMAL);
    }

    private SimplexIterationResult solvePhase1() {
        SimplexTableauSolver simplexTableauSolver = new SimplexTableauSolver(tableau, basisIndices);
        SimplexIterationResult phase1Result;
        int iteration = 1;
        do {
            phase1Result = simplexTableauSolver.solve();
            simplexStepListener.onPhase1Step(
                    phase1Result.tableau(),
                    phase1Result.enteringVar() != null ? variableNames.get(phase1Result.enteringVar()) : null,
                    phase1Result.leavingVar() != null ? variableNames.get(phase1Result.leavingVar()) : null,
                    iteration,
                    phase1Result.baseVariablesIndices()
                            .stream()
                            .map(variableNames::get)
                            .toList(),
                    variableNames);
            iteration++;
        } while (phase1Result.status() == SimplexIterationResult.Status.CONTINUE);

        if (phase1Result.status() != SimplexIterationResult.Status.OPTIMAL) {
            simplexStepListener.onPhase1Complete(phase1Result.tableau(), false);
            return phase1Result;
        }


        Fraction objectiveValue = phase1Result.tableau()
                .getEntry(
                        phase1Result.tableau().getRowDimension() - 2,
                        phase1Result.tableau().getColumnDimension() - 1);

        simplexStepListener.onPhase1Complete(
                phase1Result.tableau(),
                FractionUtils.isZero(objectiveValue));
        return phase1Result;
    }


    private List<Equation> processConstraints(List<Constraint> constraints) {
        List<Equation> equations = new ArrayList<>();
        int k = -1;

        for (Constraint constraint : constraints) {
            k++;
            if (constraint.type() == ConstraintType.EQUAL) {
                equations.add(new Equation(constraint.coefficients(), constraint.value()));
                artificialVariablesCoefficients.put(k, 1.0);
                continue;
            }

            double multiplier = (constraint.type() == ConstraintType.GREATER_THAN_OR_EQUAL) ? -1.0 :
                    1.0;
            if (multiplier * constraint.value() < 0) {
                artificialVariablesCoefficients.put(k, 1.0);
                slackVariablesCoefficients.put(k, -1.0);
                equations.add(new Equation(
                        scaleCoefficients(constraint.coefficients(), -1 * multiplier),
                        -1 * multiplier * constraint.value()));
            } else {
                equations.add(new Equation(
                        scaleCoefficients(constraint.coefficients(), multiplier),
                        multiplier * constraint.value()));
                slackVariablesCoefficients.put(k, 1.0);
            }
        }
        return equations;
    }

    private int calculateNumVariables() {
        return maxObjectiveFunctionCoefficients.size()
                + slackVariablesCoefficients.size()
                + artificialVariablesCoefficients.size();
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
        int putArtificialVar = 0;
        int putSlackVar = 0;

        for (int i = 0; i < numConstraints; i++) {
            Equation equation = equations.get(i);
            for (int j = 0; j < equation.coefficients.size(); j++) {
                tableau.setEntry(i, j, new Fraction(equation.coefficients.get(j)));
            }
            if (slackVariablesCoefficients.containsKey(i)) {
                tableau.setEntry(i, maxObjectiveFunctionCoefficients.size() + putSlackVar,
                        new Fraction(slackVariablesCoefficients.get(i)));
                putSlackVar++;
            }
            if (artificialVariablesCoefficients.containsKey(i)) {
                tableau.setEntry(i, maxObjectiveFunctionCoefficients.size()
                                + slackVariablesCoefficients.size() + putArtificialVar,
                        new Fraction(artificialVariablesCoefficients.get(i)));
                putArtificialVar++;
            }
            tableau.setEntry(i, numVariables, new Fraction(equation.value()));
        }
    }

    private void fillObjectiveFunctionRow(int numRows) {
        List<Double> objectiveFunctionCoefficients = buildObjectiveFunctionCoefficients();
        for (int i = 0; i < objectiveFunctionCoefficients.size(); i++) {
            tableau.setEntry(numRows - 1, i, new Fraction(objectiveFunctionCoefficients.get(i)));
        }
    }

    private void buildArtificialObjectiveFunctionRow(int numConstraints,
                                                     int numRows,
                                                     int numVariables) {
        for (int i = 0; i < maxObjectiveFunctionCoefficients.size() + slackVariablesCoefficients.size(); i++) {
            sumArtificialVariableRows(numConstraints, numRows, i);
        }

        sumArtificialVariableRows(numConstraints, numRows, numVariables);
    }

    private void sumArtificialVariableRows(int numConstraints, int numRows, int numVariables) {
        Fraction rhsSum = Fraction.ZERO;
        for (int j = 0; j < numConstraints; j++) {
            if (!artificialVariablesCoefficients.containsKey(j)) {
                continue;
            }
            rhsSum = rhsSum.add(tableau.getEntry(j, numVariables));
        }
        tableau.setEntry(numRows - 2, numVariables, rhsSum);
    }

    private Tableau preparePhase2Tableau(Tableau phase1Tableau) {
        return phase1Tableau.removeRowsAndColumns(
                List.of(phase1Tableau.getRowDimension() - 2),
                artificialVariablesCoefficients.keySet() //TODO: Clean tihs up
                        .stream()
                        .map(key -> key + maxObjectiveFunctionCoefficients.size() + slackVariablesCoefficients.size() - 1)
                        .toList());
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
        for (int i = 0; i < maxObjectiveFunctionCoefficients.size(); i++) {
            if (!basisIndices.contains(i)) {
                finalValues.add(Fraction.ZERO);
            } else {
                int rowIndex = basisIndices.indexOf(i);
                finalValues.add(tableau.getEntry(rowIndex, tableau.getColumnDimension() - 1));
            }
        }
        return new LinearProgramSolverOutput(finalValues, objectiveFunctionValue);
    }

    /**
     * Builds the objective function's row to be placed in the simplex tableau.
     *
     * @return
     */
    private List<Double> buildObjectiveFunctionCoefficients() {
        List<Double> coefficients = new ArrayList<>(maxObjectiveFunctionCoefficients);
        coefficients.addAll(new ArrayList<>(slackVariablesCoefficients.size()));
        coefficients.addAll(new ArrayList<>(artificialVariablesCoefficients.size()));
        return coefficients;
    }


    private List<Double> scaleCoefficients(List<Double> coefficients, double multiplier) {
        return coefficients.stream().map(coef -> coef * multiplier).toList();
    }

    /**
     * Initializes the basis indices for the simplex tableau solver,
     * For a two-phase problem, takes the positive slack variables and all artificial variables.
     * For a single-phase problem, takes all the slack variables. (which will be all positive,
     * duh!:P )
     *
     * @return
     */
    private List<Integer> initializeBasisIndices() {
        List<Integer> indices = new ArrayList<>();
        //TODO: Clean this up its embarassing
        indices.addAll(slackVariablesCoefficients.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> entry.getKey() + maxObjectiveFunctionCoefficients.size())
                .toList());
        indices.addAll(artificialVariablesCoefficients.keySet()
                .stream()
                .map(key -> key + maxObjectiveFunctionCoefficients.size() + slackVariablesCoefficients.size() - 1)
                .toList());
        indices.sort(Integer::compareTo);
        return indices;
    }

    //TODO: Do we really need this class?
    private record Equation(List<Double> coefficients, double value) {
    }

}
