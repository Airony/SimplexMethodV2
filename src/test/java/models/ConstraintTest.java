package models;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConstraintTest {

    @Test
    void testConstraintProperties() {
        Constraint constraint = new Constraint(10.0, ConstraintType.LESS_THAN_OR_EQUAL, List.of(1.0, 2.0));
        assertEquals(10.0, constraint.getValue());
        assertEquals(ConstraintType.LESS_THAN_OR_EQUAL, constraint.getType());
        assertEquals(List.of(1.0, 2.0), constraint.getCoefficients());
    }

    @Test
    void testTransformLessThanOrEqualRemainsUnchanged() {
        Constraint constraint = new Constraint(15.0, ConstraintType.LESS_THAN_OR_EQUAL, List.of(3.0, -1.0));
        constraint.transformToLessThanOrEqual();

        assertEquals(ConstraintType.LESS_THAN_OR_EQUAL, constraint.getType());
        assertEquals(15.0, constraint.getValue());
        assertEquals(List.of(3.0, -1.0), constraint.getCoefficients());
    }

    @Test
    void testTransformGreaterThanOrEqualInverts() {
        Constraint constraint = new Constraint(8.0, ConstraintType.GREATER_THAN_OR_EQUAL, List.of(2.0, -4.0));
        constraint.transformToLessThanOrEqual();

        assertEquals(ConstraintType.LESS_THAN_OR_EQUAL, constraint.getType());
        assertEquals(-8.0, constraint.getValue());
        assertEquals(List.of(-2.0, 4.0), constraint.getCoefficients());
    }

    @Test
    void testTransformEqualThrowsIllegalStateException() {
        Constraint constraint = new Constraint(5.0, ConstraintType.EQUAL, List.of(1.0, 1.0));
        assertThrows(IllegalStateException.class, constraint::transformToLessThanOrEqual);
    }
}
