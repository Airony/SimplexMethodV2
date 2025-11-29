package logic;

import org.apache.commons.math3.fraction.Fraction;

public class FractionUtils {
    static boolean isZero(Fraction fraction) {
        return fraction.getNumerator() == 0;
    }

    static boolean isStrictlyPositive(Fraction fraction) {
        return fraction.getDenominator() * fraction.getNumerator() > 0;
    }

    static boolean isPositive(Fraction fraction) {
        return fraction.getDenominator() * fraction.getNumerator() >= 0;
    }

    static boolean isNegative(Fraction fraction) {
        return fraction.getDenominator() * fraction.getNumerator() < 0;
    }

    static boolean isStrictlyNegative(Fraction fraction) {
        return fraction.getDenominator() * fraction.getNumerator() < 0;
    }


}
