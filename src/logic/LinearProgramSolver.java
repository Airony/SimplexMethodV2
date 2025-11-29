package logic;

import models.Constraint;
import models.ConstraintType;
import models.SimplexTableauSolverDTO;
import org.apache.commons.math3.fraction.Fraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinearProgramSolver {
    private final List<Double> maxObjectiveFunctionCoefficients;
    private final Map<Integer, Double> slackVariablesCoefficients = new HashMap<>();
    private final Map<Integer, Double> artificialVariablesCoefficients = new HashMap<>();
    private final Tableau tableau;
    private final List<Integer> basisIndices;

    public LinearProgramSolver(List<Constraint> constraints,
                               List<Double> maxObjectiveFunctionCoefficients) {
        this.maxObjectiveFunctionCoefficients = maxObjectiveFunctionCoefficients;

        List<Equation> equations = processConstraints(constraints);

        int numVariables = calculateNumVariables();
        int numRows = calculateNumRows(constraints.size());

        tableau = initializeTableau(numRows, numVariables + 1);

        fillConstraintRows(equations, constraints.size(), numVariables);
        fillObjectiveFunctionRow(numRows);

        if (!artificialVariablesCoefficients.isEmpty()) {
            buildArtificialObjectiveFunctionRow(constraints.size(), numRows, numVariables);
        }

        System.out.println("Initial Simplex Tableau:");
        tableau.print();

        basisIndices = initializeBasisIndices();
    }

    public LinearProgramSolverOutput solve() {
        System.out.println("Initial basis indices: " + basisIndices);
        SimplexTableauSolver simplexTableauSolver = new SimplexTableauSolver(tableau, basisIndices);
        var phase1Result = simplexTableauSolver.solve();

        if (artificialVariablesCoefficients.isEmpty()) {
            return extractSolution(phase1Result.tableau(), phase1Result.basisIndices());
        }

        Fraction objectiveValue = phase1Result.tableau()
                .getEntry(
                        phase1Result.tableau().getRowDimension() - 2,
                        phase1Result.tableau().getColumnDimension() - 1);

        if (!objectiveValue.equals(Fraction.ZERO)) {
            System.out.println("The linear program is infeasible.");
            return null;
        }

        Tableau phase2Tableau = preparePhase2Tableau(phase1Result);
        var phase2Solver = new SimplexTableauSolver(phase2Tableau, phase1Result.basisIndices());
        System.out.println("Starting Phase 2 Simplex Solver:");
        var phase2Result = phase2Solver.solve();

        return extractSolution(phase2Result.tableau(), phase2Result.basisIndices());
    }

    private List<Equation> processConstraints(List<Constraint> constraints) {
        List<Equation> equations = new ArrayList<>();
        int k = -1;

        for (Constraint constraint : constraints) {
            k++;
            if (constraint.getType() == ConstraintType.EQUAL) {
                equations.add(new Equation(constraint.getCoefficients(), constraint.getValue()));
                artificialVariablesCoefficients.put(k, 1.0);
                continue;
            }

            double multiplier = (constraint.getType() == ConstraintType.GREATER_THAN_OR_EQUAL) ? -1.0 : 1.0;
            if (multiplier * constraint.getValue() < 0) {
                artificialVariablesCoefficients.put(k, 1.0);
                slackVariablesCoefficients.put(k, -1.0);
                equations.add(new Equation(
                        scaleCoefficients(constraint.getCoefficients(), -1 * multiplier),
                        -1 * multiplier * constraint.getValue()));
            } else {
                equations.add(new Equation(
                        scaleCoefficients(constraint.getCoefficients(), multiplier),
                        multiplier * constraint.getValue()));
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

    private Tableau preparePhase2Tableau(SimplexTableauSolverDTO phase1Result) {
        return phase1Result.tableau().removeRowsAndColumns(
                List.of(phase1Result.tableau().getRowDimension() - 2),
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
