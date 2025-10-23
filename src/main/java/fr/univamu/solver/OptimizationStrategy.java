package fr.univamu.solver;


public class OptimizationStrategy extends AlwaysReduceStrategy {

    private final Variable target;
    private int bestValue = Integer.MAX_VALUE;

    public OptimizationStrategy(Variable target) {
        this.target = target;
    }

    @Override
    public boolean before(Solver solver) {
        solver.reduce();
        return solver.checkConstraints();
    }

    @Override
    public boolean check(Solver solver) {
        solver.reduce();
        if (target.getMin() > bestValue) {
            return false;
        }
        return solver.checkConstraints();
    }

    public void updateBest(Solver solver) {
        if (target.isOneValue() && target.getMin() < bestValue) {
            bestValue = target.getMin();
            target.reduce(target.getMin(), bestValue);
        }
    }
}
