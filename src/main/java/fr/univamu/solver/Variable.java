package fr.univamu.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Variable extends Interval {

	private static long anonymousCounter = 0;
	private final String name;
	private final boolean named;


	private final List<Consumer<Variable>> observers = new ArrayList<>();

	public Variable() {
		named = false;
		name = "_" + (++anonymousCounter);
	}

	public Variable(String name) {
		named = true;
		this.name = name;
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
		int oldMin = getMin();
		int oldMax = getMax();
		boolean changed = super.reduce(newMin, newMax);
		if (changed && (getMin() != oldMin || getMax() != oldMax)) {
			notifyObservers();
		}
		return changed;
	}

	public int getFixedValue() {
		if (isOneValue()) return getMin();
		throw new IllegalStateException("variable not fixed: " + this);
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
