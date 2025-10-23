package fr.univamu.solver;

public class Interval {
	public static int MIN_VALUE = -1_000_000_000;
	public static int MAX_VALUE = 1_000_000_000;

	private int min = MIN_VALUE;
	private int max = MAX_VALUE;

	public Interval() {
	}

	public Interval(int min, int max) {
		if (min <= max) {
			this.min = min;
			this.max = max;
		} else {
			this.min = 1;
			this.max = -1;
		}
	}

	public String toString() {
		return isEmpty() ? "[]" : String.format("[%d,%d]", min, max);
	}

	public boolean reduce(Interval i) {
		return reduce(i.min, i.max);
	}

	/*
	 * Réduire l'intervalle et renvoyer TRUE si une modification a été faite.
	 */
	public boolean reduce(int newMin, int newMax) {
		var oldMin = min;
		var oldMax = max;
		if (newMin > newMax) {
			min = 1;
			max = -1;
		} else {
			min = Integer.max(newMin, min);
			max = Integer.min(newMax, max);
			if (min > max) {
				min = 1;
				max = -1;
			}
		}
		return (min != oldMin || max != oldMax);
	}

	public int getSize() {
		return (min <= max) ? (1 + max - min) : 0;
	}

	public boolean isOneValue() {
		return (min == max);
	}

	public boolean isEmpty() {
		return (min > max);
	}

	public int getSign() {
		if (max < 0)
			return -1;
		if (min > 0)
			return +1;
		return 0;
	}

	public boolean isNotEmpty() {
		return (min <= max);
	}

	public boolean isInside(Interval i) {
		return (i.min <= this.min && this.min <= this.max && this.max <= i.max);
	}

	public boolean contains(int v) {
		return (min <= v && v <= max);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Interval i) {
			return (this.min == i.min && this.max == i.max);
		}
		return false;
	}

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public void init(int min, int max) {
        this.min = min;
        this.max = max;
    }

    static public Interval empty() {
		return new Interval(+1, -1);
	}

	public Interval add(Interval i) {
		if (isEmpty() || i.isEmpty()) {
			return empty();
		}
		return new Interval(this.min + i.min, this.max + i.max);
	}

	public Interval sub(Interval i) {
		if (isEmpty() || i.isEmpty()) {
			return empty();
		}
		return new Interval(this.min - i.max, this.max - i.min);
	}

	public Interval mul(Interval i) {
		if (isEmpty() || i.isEmpty()) {
			return empty();
		}
		var minMin = this.min * i.min;
		var minMax = this.min * i.max;
		var maxMin = this.max * i.min;
		var maxMax = this.max * i.max;
		int min = Math.min(Math.min(Math.min(minMin, minMax), maxMin), maxMax);
		int max = Math.max(Math.max(Math.max(minMin, minMax), maxMin), maxMax);
		return new Interval(min, max);
	}

	public Interval div(Interval i) {
		if (isEmpty() || i.isEmpty()) {
			return empty();
		}
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		int[] divisors = { i.min, -1, 1, i.max };
		for (int divisor : divisors) {
			if (divisor == 0)
				continue;
			if (!i.contains(divisor))
				continue;
			// min
			min = Math.min(min, (this.min / divisor));
			min = Math.min(min, (this.max / divisor));
			// max
			max = Math.max(max, (this.min / divisor));
			max = Math.max(max, (this.max / divisor));
		}
		return new Interval(min, max);
	}

	public Interval inverseMul(Interval i) {
		if (isEmpty() || i.isEmpty() || i.contains(0)) {
			return empty();
		}

		int[] candidates = {
				this.min / i.min, this.min / i.max,
				this.max / i.min, this.max / i.max
		};

		int newMin = Integer.MAX_VALUE;
		int newMax = Integer.MIN_VALUE;
		for (int c : candidates) {
			newMin = Math.min(newMin, c);
			newMax = Math.max(newMax, c);
		}

		return new Interval(newMin, newMax);
	}


	public Interval inter(Interval i) {
		var min = Integer.max(this.min, i.min);
		var max = Integer.min(this.max, i.max);
		return new Interval(min, max);
	}

}
