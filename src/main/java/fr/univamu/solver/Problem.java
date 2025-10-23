package fr.univamu.solver;

import java.util.List;


public record Problem(List<Variable> variables, List<Constraint> constraints) {

    @Override
    public String toString() {
        return String.format("Problem(%d variables, %d constraints)",
                variables.size(), constraints.size());
    }
}
