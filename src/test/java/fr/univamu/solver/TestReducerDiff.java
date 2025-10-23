package fr.univamu.solver;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class TestReducerDiff {

    @Test
    void testDiffCase1() {
        Variable X = new Variable("X", new Interval(10, 20));
        Variable Y = new Variable("Y", new Interval(20, 20));
        Constraint c = new Constraint(ConstraintType.NEQ, null, X, Y);

        Reducer reducer = new Reducer(List.of(c));
        reducer.reduceAll(false, List.of(X, Y));

        assertEquals("[10,19]", X.toString(), "X doit être réduit à [10,19]");
        assertEquals("[20,20]", Y.toString());
    }

    @Test
    void testDiffCase2() {
        Variable X = new Variable("X", new Interval(10, 10));
        Variable Y = new Variable("Y", new Interval(10, 11));
        Constraint c = new Constraint(ConstraintType.NEQ, null, X, Y);

        Reducer reducer = new Reducer(List.of(c));
        reducer.reduceAll(false, List.of(X, Y));

        assertEquals("[11,11]", Y.toString(), "Y doit être réduit à [11,11]");
    }

    @Test
    void testDiffCase3() {
        Variable X = new Variable("X", new Interval(15, 15));
        Variable Y = new Variable("Y", new Interval(15, 15));
        Constraint c = new Constraint(ConstraintType.NEQ, null, X, Y);

        Reducer reducer = new Reducer(List.of(c));
        reducer.reduceAll(false, List.of(X, Y));

        assertTrue(X.isEmpty() && Y.isEmpty(), "Les deux domaines doivent être vides");
    }
}
