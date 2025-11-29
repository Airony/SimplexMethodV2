package logic;

import models.Constraint;
import models.ConstraintType;
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

    public LinearProgramSolver(
            List<Constraint> constraints,
            List<Double> maxObjectiveFunctionCoefficients
    ) {
        this.maxObjectiveFunctionCoefficients = maxObjectiveFunctionCoefficients;
        // Bon, How do we do this?
        // For each constraint, if it's = we add an artificial variable with coef 1
        // If its >=, transform to <= and add artificial variable with coef 1
        // If its <=,  if the RHS is negative, we add a coef -1 and an artificial variable with
        // coef 1, else we add a slack variable with coef 1
        List<Equation> equations = new ArrayList<>();
        int k = -1;
        for (Constraint constraint : constraints) {
            k++;
            if (constraint.getType() == ConstraintType.EQUAL) {
                equations.add(new Equation(constraint.getCoefficients(), constraint.getValue()));
                artificialVariablesCoefficients.put(k, 1.0);
                continue;
            }
            double mult;
            if (constraint.getType() == ConstraintType.GREATER_THAN_OR_EQUAL) {
                mult = -1.0;
            } else {
                mult = 1.0;
            }
            if (mult * constraint.getValue() < 0) {
                artificialVariablesCoefficients.put(k, 1.0);
                slackVariablesCoefficients.put(k, -1.0);
                List<Double> newCoefficients = constraint.getCoefficients().stream()
                        .map(coef -> -mult * coef).toList();
                double newValue = -mult * constraint.getValue();

                equations.add(new Equation(
                        newCoefficients,
                        newValue));
            } else {
                equations.add(new Equation(
                        constraint.getCoefficients().stream()
                                .map(coef -> coef * mult).toList(),
                        mult * constraint.getValue()));
                slackVariablesCoefficients.put(k, 1.0);
            }
        }
        // Build the simplex tableau
        int numVariables = maxObjectiveFunctionCoefficients.size()
                + slackVariablesCoefficients.size()
                + artificialVariablesCoefficients.size();
        int numConstraints = constraints.size();
        int numRows = numConstraints + 1 + (!artificialVariablesCoefficients.isEmpty() ? 1 : 0);
        // The extra row is for two-phase simplex
        System.out.println("Num rows " + numRows);
        System.out.println("Num Cols : " + numVariables);
        System.out.println("Num Constraints: " + numConstraints);
        System.out.println("Num Slack Variables: " + slackVariablesCoefficients.size());
        System.out.println("Num Artificial Variables: " + artificialVariablesCoefficients.size());


        tableau = new Tableau(numRows, numVariables + 1);

        // Fill the tableau with zeroes
        tableau.fillZeroes();
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numVariables + 1; j++) {
                tableau.setEntry(i, j, Fraction.ZERO);
            }
        }

        // Fill the constraint rows
        int putArtificialVar = 0;
        int putSlackVar = 0;
        for (int i = 0; i < numConstraints; i++) {
            Equation equation = equations.get(i);
            for (int j = 0; j < equation.coefficients.size(); j++) {
                tableau.setEntry(i, j, new Fraction(equation.coefficients.get(j)));
            }
            if (slackVariablesCoefficients.containsKey(i)) {
                tableau.setEntry(
                        i,
                        maxObjectiveFunctionCoefficients.size() + putSlackVar,
                        new Fraction(slackVariablesCoefficients.get(i)));
                putSlackVar++;
            }
            if (artificialVariablesCoefficients.containsKey(i)) {
                tableau.setEntry(
                        i,
                        maxObjectiveFunctionCoefficients.size()
                                + slackVariablesCoefficients.size()
                                + putArtificialVar,
                        new Fraction(artificialVariablesCoefficients.get(i)));
                putArtificialVar++;
            }
            tableau.setEntry(i, numVariables, new Fraction(equation.getValue()));
        }

        List<Double> objectiveFunctionCoefficients = buildObjectiveFunctionCoefficients();
        for (int i = 0; i < objectiveFunctionCoefficients.size(); i++) {
            tableau.setEntry(numRows - 1, i, new Fraction(objectiveFunctionCoefficients.get(i)));
        }

        if (!artificialVariablesCoefficients.isEmpty()) {
            // Build the artificial objective function row

            for (int i = 0; i < maxObjectiveFunctionCoefficients.size() + slackVariablesCoefficients.size(); i++) {
                Fraction sum = Fraction.ZERO;
                for (int j = 0; j < numConstraints; j++) {
                    if (!artificialVariablesCoefficients.containsKey(j)) {
                        continue;
                    }
                    sum = sum.add(tableau.getEntry(j, i));
                }
                tableau.setEntry(numRows - 2, i, sum);
            }
            // Set RHS value
            Fraction rhsSum = Fraction.ZERO;
            for (int j = 0; j < numConstraints; j++) {
                if (!artificialVariablesCoefficients.containsKey(j)) {
                    continue;
                }
                rhsSum = rhsSum.add(tableau.getEntry(j, numVariables));
            }
            tableau.setEntry(numRows - 2, numVariables, rhsSum);
        }

        System.out.println("Initial Simplex Tableau:");
        tableau.print();

        basisIndices = new ArrayList<>();
        // Initial basis variables are the positive slack and artificial variables
        basisIndices.addAll(this.slackVariablesCoefficients.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> entry.getKey()
                        + maxObjectiveFunctionCoefficients.size()
                )
                .toList());
        basisIndices.addAll(this.artificialVariablesCoefficients.keySet()
                .stream()
                .map(key -> key + maxObjectiveFunctionCoefficients.size() + this.slackVariablesCoefficients.size() - 1)
                .toList());
        basisIndices.sort(Integer::compareTo);

    }

    public LinearProgramSolverOutput solve() {

        System.out.println("Initial basis indices: " + basisIndices);
        SimplexTableauSolver simplexTableauSolver = new SimplexTableauSolver(tableau, basisIndices);
        var phase1Result = simplexTableauSolver.solve();
        if (artificialVariablesCoefficients.isEmpty()) {
            return displaySolution(phase1Result.tableau(), phase1Result.basisIndices());
        }
        // Else, move on to phase 2
        // Check if its feasible , i.e. the objective function value is 0
        Fraction objectiveValue = phase1Result.tableau()
                .getEntry(
                        phase1Result.tableau().getRowDimension() - 2,
                        phase1Result.tableau().getColumnDimension() - 1);
        if (!objectiveValue.equals(Fraction.ZERO)) {
            System.out.println("The linear program is infeasible.");
            return null;
        }

        Tableau phase2Tableau = phase1Result.tableau().removeRowsAndColumns(
                List.of(phase1Result.tableau().getRowDimension() - 2),
                // Remove artificial objective function row
                // The last columns before RHS are the artificial variables
                artificialVariablesCoefficients.keySet()
                        .stream()
                        .map(key -> key + maxObjectiveFunctionCoefficients.size() + slackVariablesCoefficients.size() - 1)
                        .toList()

        );
        // Remove the artificial objective function row
        System.out.println("Phase 2 Tableau:");
        phase2Tableau.print();
        var phase2Solver = new SimplexTableauSolver(phase2Tableau, phase1Result.basisIndices());
        System.out.println("Starting Phase 2 Simplex Solver:");
        var phase2Result = phase2Solver.solve();
        return displaySolution(phase2Result.tableau(), phase2Result.basisIndices());
    }

    private LinearProgramSolverOutput displaySolution(Tableau tableau, List<Integer> basisIndices) {
        Fraction objectiveFunctionValue = tableau.getEntry(
                tableau.getRowDimension() - 1,
                tableau.getColumnDimension() - 1);
        System.out.println("Optimal objective function value: " + objectiveFunctionValue);
        // For each variable, check if it's in the basis
        List<Fraction> finalValues = new ArrayList<>();
        for (int i = 0; i < maxObjectiveFunctionCoefficients.size(); i++) {
            if (!basisIndices.contains(i)) {
                System.out.println("Variable x" + i + " = 0");
                finalValues.add(new Fraction(0));
            } else {
                // Find the row where it's basic
                int rowIndex = basisIndices.indexOf(i);
                Fraction value = tableau.getEntry(rowIndex, tableau.getColumnDimension() - 1);
                finalValues.add(value);
                System.out.println("Variable x" + i + " = " + value);
            }
        }
        return new LinearProgramSolverOutput(finalValues, objectiveFunctionValue);
    }

    private List<Double> buildObjectiveFunctionCoefficients() {
        List<Double> objectiveFunctionCoefficients = new ArrayList<>(
                maxObjectiveFunctionCoefficients);
        // For each slack variable, coef is 0
        for (int i = 0; i < slackVariablesCoefficients.size(); i++) {
            objectiveFunctionCoefficients.add(0.0);
        }
        // For each artificial variable, coef is 0
        for (int i = 0; i < artificialVariablesCoefficients.size(); i++) {
            objectiveFunctionCoefficients.add(0.0);
        }

        return objectiveFunctionCoefficients;
    }

    private static class Equation {
        private final List<Double> coefficients;
        private final double value;

        public Equation(List<Double> coefficients, double value) {
            this.coefficients = coefficients;
            this.value = value;
        }

        public List<Double> getCoefficients() {
            return coefficients;
        }

        public double getValue() {
            return value;
        }
    }


}
