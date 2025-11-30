package models;

import java.util.List;

public record Constraint(double value, ConstraintType type, List<Double> coefficients) {
}
