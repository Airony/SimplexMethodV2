package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConstraintTypeTest {

    @Test
    void testFromStringValid() {
        assertEquals(ConstraintType.LESS_THAN_OR_EQUAL, ConstraintType.fromString("<="));
        assertEquals(ConstraintType.GREATER_THAN_OR_EQUAL, ConstraintType.fromString(">="));
        assertEquals(ConstraintType.EQUAL, ConstraintType.fromString("="));
    }

    @Test
    void testFromStringInvalid() {
        assertThrows(IllegalArgumentException.class, () -> ConstraintType.fromString("<"));
        assertThrows(IllegalArgumentException.class, () -> ConstraintType.fromString(">"));
        assertThrows(IllegalArgumentException.class, () -> ConstraintType.fromString("!="));
        assertThrows(IllegalArgumentException.class, () -> ConstraintType.fromString(""));
    }
}
