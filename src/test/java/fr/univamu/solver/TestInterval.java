package fr.univamu.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class TestInterval {
	
	@Test
	void testEmpty() {
		var i = new Interval(20, 10);
		assertTrue(i.isEmpty());
	}

	@Test
	void testFixed() {
		var i = new Interval(20, 20);
		assertTrue(i.isOneValue());
	}

	@Test
	void testAdd() {
		var a = new Interval(20, 30);
		var b = new Interval(-33, -5);
		assertEquals("[-13,25]", a.add(b).toString());
	}
	@Test
	void testSub() {
		var a = new Interval(20, 30);
		var b = new Interval(5, 10);
		assertEquals("[10,25]", a.sub(b).toString());

		var c = new Interval(-5, 5);
		var d = new Interval(-3, 2);
		assertEquals("[-7,8]", c.sub(d).toString());
	}

	@Test
	void testMul() {
		var a = new Interval(2, 4);
		var b = new Interval(3, 5);
		assertEquals("[6,20]", a.mul(b).toString());

		var c = new Interval(-2, 3);
		var d = new Interval(-1, 2);
		assertEquals("[-4,6]", c.mul(d).toString());

		var e = new Interval(-3, -1);
		var f = new Interval(2, 4);
		assertEquals("[-12,-2]", e.mul(f).toString());
	}


	@Test
	void testDiv() {
		var a = new Interval(10, 20);
		var b = new Interval(2, 4);
		assertEquals("[2,10]", a.div(b).toString());

		var c = new Interval(-10, -5);
		var d = new Interval(2, 3);
		assertEquals("[-5,-1]", c.div(d).toString());

		var e = new Interval(-6, 6);
		var f = new Interval(-2, 2);
		var result = e.div(f);
		assertTrue(!result.isEmpty(), "Résultat doit être non vide meme avec zéro dans l'intervalle");
	}

	private Interval exploreOperation(
			BiFunction<Integer, Integer, Integer> operation,
			Interval a, Interval b) {

		if (a.isEmpty() || b.isEmpty()) {
			return Interval.empty();
		}

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;

		for (int i = a.getMin(); i <= a.getMax(); i++) {
			for (int j = b.getMin(); j <= b.getMax(); j++) {
				try {
					int result = operation.apply(i, j);
					min = Math.min(min, result);
					max = Math.max(max, result);
				} catch (ArithmeticException e) {

				}
			}
		}

		return new Interval(min, max);
	}


	private List<Interval> buildAllNotEmptyIntervals(int min, int max) {
		List<Interval> all = new ArrayList<>();
		for (int i = min; i <= max; i++) {
			for (int j = i; j <= max; j++) {
				all.add(new Interval(i, j));
			}
		}
		return all;
	}

    @Test
    void testBuildAllNotEmptyIntervals() {
        var result = buildAllNotEmptyIntervals(0, 9);
        assertEquals(55, result.size(), "Il doit y avoir 55 intervalles entre 0 et 9");
        assertEquals("[0,0]", result.get(0).toString());
        assertEquals("[0,9]", result.get(9).toString());
        assertEquals("[9,9]", result.get(result.size()-1).toString());
    }



    @Test
	void testExploreAdd() {
		Interval a = new Interval(3, 10);
		Interval b = new Interval(-5, 0);
		Interval result = exploreOperation((x,y) -> x + y, a, b);
		assertEquals("[ -2,10 ]".replace(" ",""), result.toString());
	}

	@Test
	void testExploreDiv() {
		Interval a = new Interval(-10, 10);
		Interval b = new Interval(-2, 2);
		Interval result = exploreOperation((x,y) -> x / y, a, b);
		assertTrue(!result.isEmpty(), "Résultat ne doit pas être meme avec zéro dans le diviseur");
	}



	@Test
	void testExhaustiveAddition() {
		var intervals = buildAllNotEmptyIntervals(-8, 8);
		intervals.add(Interval.empty());
		for (var a : intervals) {
			for (var b : intervals) {

				var expected = exploreOperation((x, y) -> x + y, a, b);
				var actual = a.add(b);
				assertEquals(
						expected.toString(),
						actual.toString(),
						"Erreur pour a=" + a + " et b=" + b
				);
			}
		}
	}



	private void testBiOperation(BiFunction<Integer, Integer, Integer> intOp, BiFunction<Interval, Interval, Interval> intervalOp, String name) {

		var intervals = buildAllNotEmptyIntervals(-8, 8);
		intervals.add(Interval.empty());

		for (var a : intervals) {
			for (var b : intervals) {
				var expected = exploreOperation(intOp, a, b);
				Interval actual;
				try {
					actual = intervalOp.apply(a, b);
				} catch (ArithmeticException e) {
					continue;
				}
				assertEquals(
						expected.toString(),
						actual.toString(),
						() -> String.format("Erreur")
				);
			}
		}
	}


	@Test
	void testAllOperations() {
		testBiOperation((x, y) -> x + y, (a, b) -> a.add(b), "addition");
		testBiOperation((x, y) -> x - y, (a, b) -> a.sub(b), "soustraction");
		testBiOperation((x, y) -> x * y, (a, b) -> a.mul(b), "multiplication");
		testBiOperation((x, y) -> x / y, (a, b) -> a.div(b), "division");
	}


	@Test
	void testDivByEmpty() {
		var a = new Interval(-5, 5);
		var b = Interval.empty();
		assertTrue(a.div(b).isEmpty(), "Doit être vide");
	}

	@Test
	void testEmptyDivAnything() {
		var a = Interval.empty();
		var b = new Interval(1, 3);
		assertTrue(a.div(b).isEmpty());
	}

	@Test


	public void testExpressionOptimization() {
		Solver solver = new Solver();
		Variable A = solver.newVar("A", 1, 10);
		Variable B = solver.newVar("B", 5, 15);


		Variable tmp = solver.expression(A, "+", 2);


		solver.addRelation(tmp, "=", B);


		long anonymousCount = solver.variables.stream().filter(v -> !v.isNamed()).count();
		assertTrue(anonymousCount <= 3,
				"Il doit y avoir peu de variables anonymes (très limitées), mais pas " + anonymousCount);


		assertTrue(solver.checkConstraints(), "Les contraintes doivent être vérifiées");
	}







}
