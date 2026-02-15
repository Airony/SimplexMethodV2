package models;


import logic.Tableau;

import java.util.List;

public record DualSimplexResult(Integer enteringVar, Integer leavingVar, Tableau tableau,
                                     models.DualSimplexResult.Status status,
                                     List<Integer> baseVariablesIndices) {
    public enum Status {
        Feasible,
        NonFeasible,
        CONTINUE
    }

}
