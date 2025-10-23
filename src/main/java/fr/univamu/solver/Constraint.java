package fr.univamu.solver;

public record Constraint(ConstraintType type, Variable result, Variable var1, Variable var2) {

    @Override
    public String toString() {
        return String.format("%s(%s,%s,%s)", type, result, var1, var2);
    }
}
