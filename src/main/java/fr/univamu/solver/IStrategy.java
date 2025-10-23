package fr.univamu.solver;


public interface IStrategy {

    boolean before(Solver solver);

    Variable chooseVariable(Solver solver);

    int step(Variable v);

    boolean check(Solver solver);

    void backup(Solver solver);

    void restore(Solver solver);
}
