package fr.univamu.solver;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Reducer {
    private final List<Constraint> constraints;
    private final Queue<Constraint> toProcess = new LinkedList<>();
    private boolean modified = false;


    public Reducer(List<Constraint> constraints) {
        this.constraints = constraints;
        for (Constraint c : constraints) {
            if (c.var1() != null)
                c.var1().addObserver(v -> toProcess.add(c));
            if (c.var2() != null)
                c.var2().addObserver(v -> toProcess.add(c));
            if (c.result() != null)
                c.result().addObserver(v -> toProcess.add(c));
        }
    }

    public boolean isModified() {
        return modified;
    }

    private void resetModified() {
        modified = false;
    }

    private void reduceAddConstraint(Constraint c) {
        modified = c.result().reduce(c.var1().add(c.var2())) || modified;
        modified = c.var1().reduce(c.result().sub(c.var2())) || modified;
        modified = c.var2().reduce(c.result().sub(c.var1())) || modified;
    }

    private void reduceMulConstraint(Constraint c) {
        for (int i = 0; i < 3; i++) {
            modified = c.result().reduce(c.var1().mul(c.var2())) || modified;
            modified = c.var2().reduce(c.result().inverseMul(c.var1())) || modified;
            modified = c.var1().reduce(c.result().inverseMul(c.var2())) || modified;
        }
    }

    private void reduceDivConstraint(Constraint c) {
        var a = c.var1();
        var b = c.var2();
        var r = c.result();

        if (b.contains(0)) return;

        modified = r.reduce(a.div(b)) || modified;
        modified = a.reduce(r.mul(b)) || modified;

        if (!r.contains(0)) {
            modified = b.reduce(a.div(r)) || modified;
        }
    }

    private void reduceSubConstraint(Constraint c) {
        modified = c.result().reduce(c.var1().sub(c.var2())) || modified;
        modified = c.var1().reduce(c.result().add(c.var2())) || modified;
        modified = c.var2().reduce(c.var1().sub(c.result())) || modified;
    }

    private void reduceEqConstraint(Constraint c) {
        if (c.var1() != null && c.var2() != null) {
            modified = c.var1().reduce(c.var2()) || modified;
            modified = c.var2().reduce(c.var1()) || modified;
        } else if (c.result() != null && c.var1() != null) {
            modified = c.result().reduce(c.var1()) || modified;
            modified = c.var1().reduce(c.result()) || modified;
        } else if (c.result() != null && c.var2() != null) {
            modified = c.result().reduce(c.var2()) || modified;
            modified = c.var2().reduce(c.result()) || modified;
        }
    }


    private void reduceDiffConstraint(Constraint c) {
        Variable x = c.var1();
        Variable y = c.var2();
        if (x.isOneValue() && y.isOneValue() && x.getMin() == y.getMin()) {
            x.init(1, -1); // intervalle vide
            y.init(1, -1);
            modified = true;
            return;
        }

        if (y.isOneValue()) {
            int valY = y.getMin();
            if (x.contains(valY)) {
                if (x.getMin() == valY) {
                    modified = x.reduce(valY + 1, x.getMax()) || modified;
                } else if (x.getMax() == valY) {     // ex : [10,20] → [10,19]
                    modified = x.reduce(x.getMin(), valY - 1) || modified;
                }
            }
        }

        if (x.isOneValue()) {
            int valX = x.getMin();
            if (y.contains(valX)) {
                if (y.getMin() == valX) {
                    modified = y.reduce(valX + 1, y.getMax()) || modified;
                } else if (y.getMax() == valX) {
                    modified = y.reduce(y.getMin(), valX - 1) || modified;
                }
            }
        }
        if (!x.isOneValue() && !y.isOneValue()) {

            if (x.getMin() == x.getMax() && y.contains(x.getMin())) {
                modified = y.reduce(y.getMin(), x.getMin() - 1) || modified;
            }
            if (y.getMin() == y.getMax() && x.contains(y.getMin())) {
                modified = x.reduce(x.getMin(), y.getMin() - 1) || modified;
            }
        }

    }


    private void reduce(Constraint c) {
        switch (c.type()) {
            case ADD -> reduceAddConstraint(c);
            case SUB -> reduceSubConstraint(c);
            case MUL -> reduceMulConstraint(c);
            case DIV -> reduceDivConstraint(c);
            case EQ  -> reduceEqConstraint(c);
            case NEQ -> reduceDiffConstraint(c);
            default  -> { }
        }
    }

    public void reduceAll(boolean verbose, List<Variable> variables) {
        if (verbose) {
            System.out.println("Variables avant réduction :");
            variables.forEach(System.out::println);
        }

        toProcess.addAll(constraints);

        while (!toProcess.isEmpty()) {
            Constraint c = toProcess.poll();
            resetModified();
            reduce(c);
            if (modified) {
                if (c.var1() != null) toProcess.add(c);
                if (c.var2() != null) toProcess.add(c);
                if (c.result() != null) toProcess.add(c);
            }
        }


        if (verbose) {
            System.out.println("Variables après réduction :");
            variables.forEach(System.out::println);
        }
    }
}
