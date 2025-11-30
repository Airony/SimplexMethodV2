package logic;

import models.Constraint;

import java.util.List;

/**
 * Data class representing a Linear Programming problem.
 */
public class LPProblem {

    private final double[] objectiveCoefficients;
    private final List<Constraint> constraints;
    private final ObjectiveType objectiveType;

    public LPProblem(double[] objectiveCoefficients,
                     List<Constraint> constraints,
                     ObjectiveType objectiveType) {
        this.objectiveCoefficients = objectiveCoefficients;
        this.constraints = constraints;
        this.objectiveType = objectiveType;
    }

    public double[] getObjectiveCoefficients() {
        return objectiveCoefficients;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public ObjectiveType getObjectiveType() {
        return objectiveType;
    }

    public int getNumVariables() {
        return objectiveCoefficients.length;
    }

    public int getNumConstraints() {
        return constraints.size();
    }


    public enum ObjectiveType {
        MAXIMIZE("MAX"),
        MINIMIZE("MIN");

        private final String label;

        ObjectiveType(String label) {
            this.label = label;
        }

        public static ObjectiveType fromString(String label) {
            for (ObjectiveType type : values()) {
                if (type.label.equals(label)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown objective type: " + label);
        }

        public String getLabel() {
            return label;
        }

    }
}