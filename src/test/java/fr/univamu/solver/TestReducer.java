package fr.univamu.solver;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class TestReducer {

    @Test
    void testAddReductionInconsistent() {
        Variable A = new Variable("A", new Interval(0, 5));
        Variable B = new Variable("B", new Interval(0, 5));
        Variable R = new Variable("R", new Interval(20, 30));
        Constraint c = new Constraint(ConstraintType.ADD, R, A, B);

        Reducer reducer = new Reducer(List.of(c));
        reducer.reduceAll(false, List.of(A, B, R));
        assertTrue(A.isEmpty() || B.isEmpty() || R.isEmpty());
    }

    @Test
    void testMulReduction() {
        Variable A = new Variable("A", new Interval(2, 4));
        Variable B = new Variable("B", new Interval(3, 5));
        Variable R = new Variable("R", new Interval(6, 10));

        Constraint c = new Constraint(ConstraintType.MUL, R, A, B);
        Reducer reducer = new Reducer(List.of(c));
        reducer.reduceAll(false, List.of(A, B, R));

        assertTrue(R.getMin() >= 6 && R.getMax() <= 20);
    }
}
