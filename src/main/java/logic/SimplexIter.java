package logic;


import java.util.List;

public class SimplexIter {
    public final Integer enteringVar;
    public final Integer leavingVar;
    public final Tableau tableau;
    public final Status status;
    private final List<Integer> baseVariablesIndices;

    public SimplexIter(Integer enteringVar,
                       Integer leavingVar,
                       Tableau tableau,
                       Status status,
                       List<Integer> baseVariablesIndices) {
        this.enteringVar = enteringVar;
        this.leavingVar = leavingVar;
        this.tableau = tableau;
        this.status = status;
        this.baseVariablesIndices = baseVariablesIndices;
    }

    public List<Integer> getBaseVariablesIndices() {
        return baseVariablesIndices;
    }

    public Status getStatus() {
        return status;
    }

    public Tableau getTableau() {
        return tableau;
    }

    public Integer getLeavingVar() {
        return leavingVar;
    }

    public Integer getEnteringVar() {
        return enteringVar;
    }


    public enum Status {
        OPTIMAL,
        UNBOUNDED,
        CONTINUE
    }

}
