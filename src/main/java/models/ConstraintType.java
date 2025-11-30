package models;

public enum ConstraintType {
    LESS_THAN_OR_EQUAL,
    GREATER_THAN_OR_EQUAL,
    EQUAL;

    public static ConstraintType fromString(String value) {
        switch (value) {
            case "<=":
                return LESS_THAN_OR_EQUAL;
            case ">=":
                return GREATER_THAN_OR_EQUAL;
            case "=":
                return EQUAL;
            default:
                throw new IllegalArgumentException("Unknown constraint type: " + value);
        }

    }
}
