package models;


import logic.Tableau;

import java.util.List;

public record SimplexIterationResult(Integer enteringVar, Integer leavingVar, Tableau tableau,
                                     models.SimplexIterationResult.Status status,
                                     List<Integer> baseVariablesIndices) {
    public enum Status {
        OPTIMAL,
        UNBOUNDED,
        CONTINUE
    }

}
