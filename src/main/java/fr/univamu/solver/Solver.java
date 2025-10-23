package fr.univamu.solver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Solver implements ISolver {

    private final Solutions solutions = new Solutions();
    private Checker checker;
    private Reducer reducer;
    private IStrategy strategy = new DefaultStrategy();
    private final List<Constraint> constraints = new LinkedList<>();
    final List<Variable> variables = new LinkedList<>();
    private long solutionsCounter = 0;
    private long nodesCounter = 0;
    private long maxNodes = 1000_000_000L;
    private boolean verbose = false;
    private boolean modified = false;

    public void reduceAndCheckIntervalsStrategy() {
        this.strategy = new ReduceAndCheckStrategy();
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
        if (b.contains(0)) {
            return;
        }
        modified = r.reduce(a.div(b)) || modified;
        modified = a.reduce(r.mul(b)) || modified;
        if (!r.contains(0)) {
            modified = b.reduce(a.div(r)) || modified;
        }
    }


    private void reduce(Constraint c) {
        switch (c.type()) {
            case SUB :
                modified = c.result().reduce(c.var1().sub(c.var2())) || modified;
                modified = c.var1().reduce(c.result().add(c.var2())) || modified;
                modified = c.var2().reduce(c.var1().sub(c.result())) || modified;
                break;
            case ADD:
                reduceAddConstraint(c);
                break;
            case MUL:
                reduceMulConstraint(c);
                break;
            case DIV:
                reduceDivConstraint(c);
                break;
            case EQ:
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
                break;
            default:
                break;
        }
    }


    public void reduce() {
        if (reducer == null) {
            reducer = new Reducer(constraints);
        }
        reducer.reduceAll(verbose, variables);
    }


    boolean checkConstraints() {
        return checker.checkAll();
    }


    Variable findVariable() {
        Variable best = null;
        for (Variable v : variables) {
            if (v.isOneValue()) continue;
            if (best == null) {
                best = v;
            } else if (v.getSize() < best.getSize()) {
                best = v;
            }
        }
        return best;
    }


    private void findSolutions() {
        if (++nodesCounter > maxNodes) {
            throw new IllegalStateException("too many nodes");
        }
        if (!strategy.check(this)) {
            return;
        }
        var v = strategy.chooseVariable(this);
        if (v == null) {
            solutions.addSolution(variables);
            if (strategy instanceof OptimizationStrategy optStrategy) {
                optStrategy.updateBest(this);
            }
            solutionsCounter++;
            return;
        }
        int step = strategy.step(v);
        if (step <= 0) step = 1;
        if (v.getSize() <= 0) return;
        int min = v.getMin();
        int max = v.getMax();
        if (v.getSize() > 1000) {
            if (min < 0 && max >= 0) {
                step = -min;
            } else {
                int mid = (min + max) / 2;
                step = (1 + mid - min);
            }
        }
        if (strategy instanceof AlwaysReduceStrategy arStrategy) {
            arStrategy.backup(this);
        }
        for (int value = min; value <= max; value += step) {
            v.init(value, Math.min(value + step - 1, max));
            findSolutions();
            if (strategy instanceof AlwaysReduceStrategy arStrategy) {
                arStrategy.restore(this);
            }
        }
        v.init(min, max);
    }


    private Variable newVar(int min, int max) {
        var v = new Variable();
        variables.add(v);
        v.init(min, max);
        return v;
    }


    private Variable newVar() {
        return newVar(Variable.MIN_VALUE, Variable.MAX_VALUE);
    }


    public Variable newVar(String name, int min, int max) {
        var v = new Variable(name);
        v.init(min, max);
        variables.add(v);
        return v;
    }


    public Variable newConstant(int value) {
        return newVar(value, value);
    }


    private void eq(Variable a, Variable b) {
        constraints.add(new Constraint(ConstraintType.EQ, a, b, null));
    }


    private void gt(Variable a, Variable b) {
        sub(newVar(1, Variable.MAX_VALUE), a, b);
    }


    private void get(Variable a, Variable b) {
        sub(newVar(0, Variable.MAX_VALUE), a, b);
    }


    private void lt(Variable a, Variable b) {
        gt(b, a);
    }


    private void let(Variable a, Variable b) {
        get(b, a);
    }


    public void addAllDiffRelation(Variable... variables) {
        for (int i = 0; i < variables.length; i++)
            for (int j = i + 1; j < variables.length; j++) {
                diff(variables[i], variables[j]);
            }
    }


    private void add(Variable result, Variable a, Variable b) {
        constraints.add(new Constraint(ConstraintType.ADD, result, a, b));
    }


    private void sub(Variable result, Variable a, Variable b) {
        constraints.add(new Constraint(ConstraintType.SUB, result, a, b));
    }


    private void mul(Variable result, Variable a, Variable b) {
        constraints.add(new Constraint(ConstraintType.MUL, result, a, b));
    }


    private void div(Variable result, Variable a, Variable b) {
        constraints.add(new Constraint(ConstraintType.DIV, result, a, b));
    }


    private void diff(Variable a, Variable b) {
        constraints.add(new Constraint(ConstraintType.NEQ, a, b, null));
    }


    private Variable parseSimpleTerm(List<Object> terms) {
        var first = terms.removeFirst();
        if (first instanceof Variable var) {
            return var;
        }
        if (first instanceof Integer cst) {
            return this.newConstant(cst);
        }
        throw new IllegalArgumentException("bad expression: " + first);
    }


    private boolean parseToken(String token, List<Object> terms) {
        if (!terms.isEmpty()) {
            if (token.equals(terms.getFirst())) {
                terms.removeFirst();
                return true;
            }
        }
        return false;
    }


    private Variable parseMultiplicationTerm(List<Object> terms) {

        Variable first = parseSimpleTerm(terms);


        if (parseToken("*", terms)) {
            Variable second = parseMultiplicationTerm(terms);

            if (second.isOneValue() && second.getMin() == 1) {
                return first;
            }
            Variable result = newVar();
            mul(result, first, second);
            return result;
        }
        if (parseToken("/", terms)) {
            Variable second = parseMultiplicationTerm(terms);
            if (second.isOneValue() && second.getMin() == 1) {
                return first;
            }
            Variable result = newVar();
            div(result, first, second);
            return result;
        }
        return first;
    }


    private Variable parseAdditionTerm(List<Object> terms) {
        Variable first = parseMultiplicationTerm(terms);
        if (parseToken("+", terms)) {
            Variable second = parseAdditionTerm(terms);
            if (second.isOneValue() && second.getMin() == 0) {
                return first;
            }
            Variable result = newVar();
            add(result, first, second);
            return result;
        }

        if (parseToken("-", terms)) {
            Variable second = parseAdditionTerm(terms);
            if (second.isOneValue() && second.getMin() == 0) {
                return first;
            }
            Variable result = newVar();
            Variable neg = newVar();
            Variable zero = newVar();
            in(zero, 0, 0);
            sub(neg, zero, second);

            add(result, first, neg);
            return result;
        }
        return first;
    }


    public void addRelation(Variable a, String relation, int constant) {
        if (checker == null) {
            checker = new Checker(constraints);
        }
        addRelation(a, relation, newConstant(constant));
    }

    public void addRelation(Variable a, String relation, Variable b) {
        if (checker == null) {
            checker = new Checker(constraints);
        }
        switch (relation) {
            case "=":
                eq(a, b);
                break;
            case ">":
                gt(a, b);
                break;
            case ">=":
                get(a, b);
                break;
            case "<":
                lt(a, b);
                break;
            case "<=":
                let(a, b);
                break;
            case "<>":
                diff(a, b);
                break;
            default:
                throw new IllegalArgumentException("bad relation: " + relation);
        }
        simplifyAnonymousVariables();
    }

    public Variable expression(Object... terms) {
        var termsList = new LinkedList<>(List.of(terms));
        var result = parseAdditionTerm(termsList);
        if (!termsList.isEmpty()) {
            throw new IllegalArgumentException("bad expression: " + termsList);
        }
        return result;
    }

    public long solve() {
        this.solutions.clear();
        this.solutionsCounter = 0;
        this.nodesCounter = 0;
        this.checker = new Checker(constraints);
        if (!strategy.before(this)) return 0;
        findSolutions();
        return solutions.count();
    }


    public long getNodesCounter() {
        return nodesCounter;
    }

    public void setMaxNodes(long maxNodes) {
        this.maxNodes = maxNodes;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public Solutions getSolutions() {
        return solutions;
    }

    public void in(Variable var, int min, int max) {
        var.init(min, max);
        variables.add(var);
    }


    private void simplifyAnonymousVariables() {
        List<Constraint> newConstraints = new ArrayList<>();
        for (Constraint c : constraints) {
            if (c.result() != null && !c.result().isNamed()) {
                boolean usedElsewhere = constraints.stream().anyMatch(other ->
                        other != c && (other.var1() == c.result() || other.var2() == c.result()));
                if (!usedElsewhere) continue;
            }
            newConstraints.add(c);
        }
        constraints.clear();
        constraints.addAll(newConstraints);
    }

    public void alwaysReduceStrategy() {
        this.strategy = new AlwaysReduceStrategy();
    }

    public void optimizationStrategy(Variable target) {
        this.strategy = new OptimizationStrategy(target);
    }

    public ISolver buildAssignmentProblem(int[][] costs) {
        var solver = new Solver();
        int nbTasks = costs.length;
        int nbAgents = costs[0].length;
        Variable[][] matrix = new Variable[nbTasks][nbAgents];

        for (int t = 0; t < nbTasks; t++) {
            for (int a = 0; a < nbAgents; a++) {
                matrix[t][a] = solver.newVar("T" + t + "A" + a, 0, 1);
            }
        }

        var zero = solver.newConstant(0);
        for (int t = 0; t < nbTasks; t++) {
            var sum = zero;
            for (int a = 0; a < nbAgents; a++) {
                sum = solver.expression(sum, "+", matrix[t][a]);
            }
            solver.addRelation(sum, "=", 1);
        }

        for (int a = 0; a < nbAgents; a++) {
            var sum = zero;
            for (int t = 0; t < nbTasks; t++) {
                sum = solver.expression(sum, "+", matrix[t][a]);
            }
            solver.addRelation(sum, "<=", 1);
        }

        var costExpr = zero;
        for (int t = 0; t < nbTasks; t++) {
            for (int a = 0; a < nbAgents; a++) {
                costExpr = solver.expression(costExpr, "+", matrix[t][a], "*", costs[t][a]);
            }
        }

        var costVar = solver.newVar("COST", 0, 9999);
        solver.addRelation(costExpr, "=", costVar);

        solver.setVerbose(false);
        solver.optimizationStrategy(costVar);

        return solver;
    }

    public ISolver makeQueens(int n) {
        ISolver solver = new Solver();
        var queens = new Variable[n];
        var diagonals1 = new Variable[n];
        var diagonals2 = new Variable[n];
        for (int i = 0; i < n; i++) {
            queens[i] = solver.newVar("Q" + i, 1, n);
            diagonals1[i] = solver.expression(queens[i], "+", i);
            diagonals2[i] = solver.expression(queens[i], "-", i);
        }
        solver.addAllDiffRelation(queens);
        solver.addAllDiffRelation(diagonals1);
        solver.addAllDiffRelation(diagonals2);
        solver.alwaysReduceStrategy();
        solver.setVerbose(false);
        return solver;
    }



}

