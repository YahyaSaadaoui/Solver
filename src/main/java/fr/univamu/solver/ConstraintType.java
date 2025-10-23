package fr.univamu.solver;

public enum ConstraintType {
    ADD('+'),
    SUB('-'),
    MUL('*'),
    DIV('/'),
    EQ('='),
    NEQ('#'), // Not equal (<>)
    GT('>'),
    GTE('≥'),
    LT('<'),
    LTE('≤');

    private final char symbol;

    ConstraintType(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return Character.toString(symbol);
    }

    public static ConstraintType fromChar(char c) {
        return switch (c) {
            case '+' -> ADD;
            case '-' -> SUB;
            case '*' -> MUL;
            case '/' -> DIV;
            case '=' -> EQ;
            case '#' -> NEQ;
            case '>' -> GT;
            case '≥' -> GTE;
            case '<' -> LT;
            case '≤' -> LTE;
            default -> throw new IllegalArgumentException("Unknown constraint type: " + c);
        };
    }
}
