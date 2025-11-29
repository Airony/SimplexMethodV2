package models;

import java.util.List;

public class Constraint {
    private final List<Double> coefficients;
    private final double value;
    private final ConstraintType type;


    public Constraint(List<Double> coefficients, double value, ConstraintType type) {
        this.coefficients = coefficients;
        this.value = value;
        this.type = type;
    }

    public List<Double> getCoefficients() {
        return coefficients;
    }

    public double getValue() {
        return value;
    }

    public ConstraintType getType() {
        return type;
    }
}
