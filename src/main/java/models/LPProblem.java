package models;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Data class representing a Linear Programming problem.
 */
@RequiredArgsConstructor
@Data
public class LPProblem {

    private final double[] objectiveCoefficients;
    private final List<Constraint> constraints;
    private final ObjectiveType objectiveType;


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
    }
}