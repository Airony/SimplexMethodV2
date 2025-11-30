package models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public final class Constraint {
    private double value;
    private ConstraintType type;
    private List<Double> coefficients;

    public void transformToLessThanOrEqual() {
        if (this.type == ConstraintType.LESS_THAN_OR_EQUAL) {
            return; // No transformation needed
        }
        if (this.type == ConstraintType.EQUAL) {
            throw new IllegalStateException(
                    "Cannot transform EQUAL constraint to LESS_THAN_OR_EQUAL.");
        }

        if (this.type == ConstraintType.GREATER_THAN_OR_EQUAL) {
            this.type = ConstraintType.LESS_THAN_OR_EQUAL;
            this.coefficients = this.coefficients.stream().map(coef -> -coef).toList();
            this.value = -this.value;
        }
    }
}
