package models;

import org.apache.commons.math3.fraction.Fraction;

import java.util.List;

public record LinearProgramSolverOutput(List<Fraction> variableValues, Fraction optimalValue) {
}
