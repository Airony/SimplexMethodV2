package models;

import logic.Tableau;

import java.util.List;

public record SimplexTableauSolverDTO(Tableau tableau, List<Integer> basisIndices) {
}