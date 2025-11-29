package models;

import org.ejml.simple.SimpleMatrix;

import java.util.List;

public record SimplexTableauSolverDTO(SimpleMatrix tableau, List<Integer> basisIndices) {
}