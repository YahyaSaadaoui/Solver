package fr.univamu.solver;

import java.util.List;

public class Checker {

    private final List<Constraint> constraints;
    private long checksCount = 0;

    public Checker(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    public long getChecksCount() {
        return checksCount;
    }

    public void resetCounter() {
        checksCount = 0;
    }

    public boolean checkConstraint(Constraint c) {
        checksCount++;
        return switch (c.type()) {
            case ADD -> c.var1().add(c.var2()).inter(c.result()).isNotEmpty();
            case SUB -> c.var1().sub(c.var2()).inter(c.result()).isNotEmpty();
            case MUL -> c.var1().mul(c.var2()).inter(c.result()).isNotEmpty();
            case DIV -> c.var1().div(c.var2()).inter(c.result()).isNotEmpty();
            case NEQ -> checkDiff(c);
            case EQ -> checkEq(c);
            default -> throw new IllegalArgumentException("Unsupported constraint type: " + c);
        };
    }

    private boolean checkEq(Constraint c) {
        if (c.var1() != null && c.var2() != null) {
            return c.var1().inter(c.var2()).isNotEmpty();
        } else if (c.result() != null && c.var1() != null) {
            return c.result().inter(c.var1()).isNotEmpty();
        } else if (c.result() != null && c.var2() != null) {
            return c.result().inter(c.var2()).isNotEmpty();
        }
        return false;
    }

    private boolean checkDiff(Constraint c) {
        var a = c.var1();
        var b = c.var2();
        if (a == null || b == null) return true;
        return !(a.isOneValue() && b.isOneValue() && a.getMin() == b.getMin());
    }

    public boolean checkAll() {
        for (Constraint c : constraints) {
            if (!checkConstraint(c)) {
                return false;
            }
        }
        return true;
    }
}
