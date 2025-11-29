package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class Constraint {
    List<Double> coefficients;
    double value;
    ConstraintType type;

    public void transformToLessThenOrEqual() {
        if (this.type == ConstraintType.EQUAL) {
            throw new IllegalStateException(
                    "Cannot transform EQUAL constraint to LESS_THAN_OR_EQUAL.");
        }
        if (this.type == ConstraintType.LESS_THAN_OR_EQUAL) {
            return;
        }

        this.coefficients = this.coefficients.stream().map(coef -> -coef).toList();
        this.value = -this.value;
        this.type = ConstraintType.LESS_THAN_OR_EQUAL;
    }

}
