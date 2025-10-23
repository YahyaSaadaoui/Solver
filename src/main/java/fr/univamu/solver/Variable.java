package fr.univamu.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Variable extends Interval {

	private static long anonymousCounter = 0;
	private final String name;
	private final boolean named;
	private int min;
	private int max;
	private final List<Consumer<Variable>> observers = new ArrayList<>();

	public static final int MIN_VALUE = -999999;
	public static final int MAX_VALUE = 999999;


	public Variable() {
		named = false;
		name = "_" + (++anonymousCounter);
	}

	public Variable(String name) {
		named = true;
		this.name = name;
	}

	public void init(int min, int max) {
		this.min = min;
		this.max = max;
		notifyObservers();
	}

	public Variable(String name, Interval domain) {
		super(domain.getMin(), domain.getMax());
		this.named = true;
		this.name = name;
	}

	public void addObserver(Consumer<Variable> observer) {
		observers.add(observer);
	}

	private void notifyObservers() {
		for (Consumer<Variable> obs : observers) {
			obs.accept(this);
		}
	}

	@Override
	public boolean reduce(int newMin, int newMax) {
		if (newMin > max || newMax < min) {
			min = 1; max = -1;            // empty interval
			notifyObservers();
			return true;
		}
		boolean changed = false;
		if (newMin > min) { min = newMin; changed = true; }
		if (newMax < max) { max = newMax; changed = true; }
		if (changed) notifyObservers();
		return changed;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public int getSize() {
		return (max >= min) ? (max - min + 1) : 0;
	}

	public boolean contains(int value) {
		return value >= min && value <= max;
	}
	public int getFixedValue() {
		if (isOneValue()) return getMin();
		throw new IllegalStateException("variable not fixed: " + this);
	}

	public boolean reduceExcept(int val) {
		if (!contains(val)) return false;
		if (getSize() == 1) return false;
		if (val == min) return reduce(val + 1, max);
		if (val == max) return reduce(min, val - 1);
		return reduce(min, val - 1) || reduce(val + 1, max);
	}

	@Override
	public String toString() {
		return "[" + getMin() + "," + getMax() + "]";
	}



	public String getName() {
		return name;
	}

	public boolean isNamed() {
		return named;
	}
}
