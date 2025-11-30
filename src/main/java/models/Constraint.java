package models;

import java.util.ArrayList;
import java.util.List;

public class Constraint {
    private final double value;
    private final ConstraintType type;
    private final List<Double> coefficients;

    public Constraint() {
        this.value = 0;
        this.type = ConstraintType.LESS_THAN_OR_EQUAL;
        this.coefficients = new ArrayList<>();
    }

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
