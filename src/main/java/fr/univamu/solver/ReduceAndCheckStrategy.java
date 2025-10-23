package fr.univamu.solver;


public class ReduceAndCheckStrategy extends DefaultStrategy {

    @Override
    public boolean before(Solver solver) {
        solver.reduce();
        return true;
    }
    @Override
    public boolean check(Solver solver) {
        return solver.checkConstraints();
    }
}
