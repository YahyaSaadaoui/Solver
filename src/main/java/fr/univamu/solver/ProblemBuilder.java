package fr.univamu.solver;

import java.util.ArrayList;
import java.util.List;


public class ProblemBuilder {

    private final List<Variable> variables = new ArrayList<>();
    private final List<Constraint> constraints = new ArrayList<>();


    public Variable makeVar(String name, int min, int max) {
        Variable v = new Variable(name, new Interval(min, max));
        variables.add(v);
        return v;
    }


    public Constraint makeConstraint(ConstraintType type, Variable result, Variable v1, Variable v2) {
        Constraint c = new Constraint(type, result, v1, v2);
        constraints.add(c);
        return c;
    }

    public Problem build() {
        return new Problem(List.copyOf(variables), List.copyOf(constraints));
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }
}
