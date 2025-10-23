package fr.univamu.solver;


public class AlwaysReduceStrategy implements IStrategy {

    private Backup backup;

    @Override
    public boolean before(Solver solver) {
        solver.reduce();
        return solver.checkConstraints();
    }

    @Override
    public boolean check(Solver solver) {
        solver.reduce();
        return solver.checkConstraints();
    }

    @Override
    public Variable chooseVariable(Solver solver) {
        return solver.findVariable();
    }

    @Override
    public int step(Variable v) {
        return 1;
    }

    @Override
    public void backup(Solver solver) {
        backup = new Backup(solver.variables);
    }

    @Override
    public void restore(Solver solver) {
        if (backup != null) {
            backup.restore();
        }
    }
}
