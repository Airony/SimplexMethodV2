package logic;

import org.apache.commons.math3.fraction.Fraction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FractionUtilsTest {

    @Test
    void testIsZero() {
        assertTrue(FractionUtils.isZero(Fraction.ZERO));
        assertTrue(FractionUtils.isZero(new Fraction(0, 7)));
        assertFalse(FractionUtils.isZero(new Fraction(1, 3)));
        assertFalse(FractionUtils.isZero(new Fraction(-1, 3)));
    }

    @Test
    void testIsStrictlyPositive() {
        assertTrue(FractionUtils.isStrictlyPositive(new Fraction(3, 4)));
        assertTrue(FractionUtils.isStrictlyPositive(new Fraction(-3, -4)));
        assertFalse(FractionUtils.isStrictlyPositive(Fraction.ZERO));
        assertFalse(FractionUtils.isStrictlyPositive(new Fraction(-1, 2)));
        assertFalse(FractionUtils.isStrictlyPositive(new Fraction(1, -2)));
    }

    @Test
    void testIsPositive() {
        assertTrue(FractionUtils.isPositive(new Fraction(5, 2)));
        assertTrue(FractionUtils.isPositive(Fraction.ZERO));
        assertFalse(FractionUtils.isPositive(new Fraction(-5, 2)));
    }

    @Test
    void testIsNegative() {
        assertTrue(FractionUtils.isNegative(new Fraction(-2, 3)));
        assertTrue(FractionUtils.isNegative(new Fraction(2, -3)));
        assertFalse(FractionUtils.isNegative(Fraction.ZERO));
        assertFalse(FractionUtils.isNegative(new Fraction(2, 3)));
    }

    @Test
    void testIsStrictlyNegative() {
        assertTrue(FractionUtils.isStrictlyNegative(new Fraction(-7, 8)));
        assertFalse(FractionUtils.isStrictlyNegative(Fraction.ZERO));
        assertFalse(FractionUtils.isStrictlyNegative(new Fraction(7, 8)));
    }
}
