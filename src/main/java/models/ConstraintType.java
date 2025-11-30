package models;

public enum ConstraintType {
    LESS_THAN_OR_EQUAL,
    GREATER_THAN_OR_EQUAL,
    EQUAL;

    public static ConstraintType fromString(String value) {
        return switch (value) {
            case "<=" -> LESS_THAN_OR_EQUAL;
            case ">=" -> GREATER_THAN_OR_EQUAL;
            case "=" -> EQUAL;
            default -> throw new IllegalArgumentException("Unknown constraint type: " + value);
        };

    }

    /**
     * Enumeration of possible solution statuses.
     */
    public enum SolutionStatus {
        OPTIMAL,
        UNBOUNDED,
        INFEASIBLE,
        NEED_PHASE2
    }
}
