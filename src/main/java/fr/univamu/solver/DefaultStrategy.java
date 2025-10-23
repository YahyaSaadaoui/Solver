package fr.univamu.solver;

public class DefaultStrategy implements IStrategy {

    @Override
    public boolean before(Solver solver) {
        return true;
    }

    @Override
    public Variable chooseVariable(Solver solver) {
        return solver.findVariable();
    }

    @Override
    public int step(Variable v) {
        if (v.getSize() > 1000) {
            int min = v.getMin();
            int max = v.getMax();
            if (min < 0 && max >= 0) {
                return -min;
            } else {
                int mid = (min + max) / 2;
                return 1 + mid - min;
            }
        }
        return 1;
    }

    @Override
    public boolean check(Solver solver) {
        return solver.checkConstraints();
    }

    @Override
    public void backup(Solver solver) {

    }

    @Override
    public void restore(Solver solver) {

    }
}
