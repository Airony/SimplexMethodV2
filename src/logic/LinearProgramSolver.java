package logic;

import models.Constraint;
import models.ConstraintType;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinearProgramSolver {
    private final List<Double> maxObjectiveFunctionCoefficients;
    private final Map<Integer, Double> slackVariablesCoefficients = new HashMap<>();
    private final Map<Integer, Double> artificialVariablesCoefficients = new HashMap<>();

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


        SimpleMatrix mat = new SimpleMatrix(numRows, numVariables + 1);
        // Start with all zeroes
        mat.fill(0.0);

        // Fill the constraint rows
        int putArtificialVar = 0;
        int putSlackVar = 0;
        for (int i = 0; i < numConstraints; i++) {
            Equation equation = equations.get(i);
            for (int j = 0; j < equation.coefficients.size(); j++) {
                mat.set(i, j, equation.coefficients.get(j));
            }
            if (slackVariablesCoefficients.containsKey(i)) {
                // Set slack variable coef
                mat.set(
                        i,
                        maxObjectiveFunctionCoefficients.size() + putSlackVar,
                        slackVariablesCoefficients.get(i));
                putSlackVar++;
            }
            // Set artificial variable coef
            if (artificialVariablesCoefficients.containsKey(i)) {
                mat.set(
                        i,
                        maxObjectiveFunctionCoefficients.size()
                                + slackVariablesCoefficients.size()
                                + putArtificialVar,
                        artificialVariablesCoefficients.get(i));
                putArtificialVar++;
            }

            // Set RHS value
            mat.set(i, numVariables, equation.getValue());
        }
        List<Double> objectiveFunctionCoefficients = buildObjectiveFunctionCoefficients();
        for (int i = 0; i < objectiveFunctionCoefficients.size(); i++) {
            mat.set(numRows - 1, i, objectiveFunctionCoefficients.get(i));
        }
        if (!artificialVariablesCoefficients.isEmpty()) {
            // Build the artificial objective function row

            for (int i = 0; i < maxObjectiveFunctionCoefficients.size() + slackVariablesCoefficients.size(); i++) {
                double sum = 0.0;
                for (int j = 0; j < numConstraints; j++) {
                    if (!artificialVariablesCoefficients.containsKey(j)) {
                        continue;
                    }
                    sum += mat.get(j, i);
                }
                mat.set(numRows - 2, i, sum);
            }
            // Set RHS value
            double rhsSum = 0.0;
            for (int j = 0; j < numConstraints; j++) {
                if (!artificialVariablesCoefficients.containsKey(j)) {
                    continue;
                }
                rhsSum += mat.get(j, numVariables);
            }
            mat.set(numRows - 2, numVariables, rhsSum);
        }

        System.out.println("Initial Simplex Tableau:");
        mat.print();
        List<Integer> basisIndices = new ArrayList<>();
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
                .map(key -> key + maxObjectiveFunctionCoefficients.size() + this.slackVariablesCoefficients.size())
                .toList());
        basisIndices.sort(Integer::compareTo);

        System.out.println("Initial basis indices: " + basisIndices);
//        SimplexTableauSolver simplexTableauSolver = new SimplexTableauSolver(mat, basisIndices);
//        var phase1Result = simplexTableauSolver.solve();

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
