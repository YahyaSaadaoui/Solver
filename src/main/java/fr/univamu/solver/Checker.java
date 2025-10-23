package fr.univamu.solver;

import java.util.List;

public class Checker {
    private final List<Constraint> constraints;

    public Checker(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    public boolean checkAll() {
        for (Constraint c : constraints) {
            if (!check(c)) return false;
        }
        return true;
    }

    private static boolean intervalsOverlap(int aMin, int aMax, int bMin, int bMax) {
        return !(aMax < bMin || bMax < aMin);
    }

    private boolean check(Constraint c) {
        final Variable r = c.result();
        final Variable a = c.var1();
        final Variable b = c.var2();

        switch (c.type()) {
            case EQ: {
                if (a != null && b != null) {
                    if (a.isOneValue() && b.isOneValue()) return a.getMin() == b.getMin();
                    return intervalsOverlap(a.getMin(), a.getMax(), b.getMin(), b.getMax());
                }
                if (r != null && a != null) {
                    if (r.isOneValue() && a.isOneValue()) return r.getMin() == a.getMin();
                    return intervalsOverlap(r.getMin(), r.getMax(), a.getMin(), a.getMax());
                }
                if (r != null && b != null) {
                    if (r.isOneValue() && b.isOneValue()) return r.getMin() == b.getMin();
                    return intervalsOverlap(r.getMin(), r.getMax(), b.getMin(), b.getMax());
                }
                return true;
            }

            case NEQ: {
                if (a == null || b == null) return true;
                if (a.isOneValue() && b.isOneValue()) return a.getMin() != b.getMin();
                return true;
            }

            case ADD: {
                if (a == null || b == null || r == null) return true;
                int sMin = safeAdd(a.getMin(), b.getMin());
                int sMax = safeAdd(a.getMax(), b.getMax());
                return intervalsOverlap(r.getMin(), r.getMax(), sMin, sMax);
            }

            case SUB: {
                if (a == null || b == null || r == null) return true;
                int dMin = safeSub(a.getMin(), b.getMax());
                int dMax = safeSub(a.getMax(), b.getMin());
                return intervalsOverlap(r.getMin(), r.getMax(), dMin, dMax);
            }

            case MUL: {
                if (a == null || b == null || r == null) return true;
                long p1 = 1L * a.getMin() * b.getMin();
                long p2 = 1L * a.getMin() * b.getMax();
                long p3 = 1L * a.getMax() * b.getMin();
                long p4 = 1L * a.getMax() * b.getMax();
                long mMin = Math.min(Math.min(p1, p2), Math.min(p3, p4));
                long mMax = Math.max(Math.max(p1, p2), Math.max(p3, p4));
                return intervalsOverlap(r.getMin(), r.getMax(), clampToInt(mMin), clampToInt(mMax));
            }

            case DIV: {
                if (a == null || b == null || r == null) return true;
                // if b can be zero, we can't decide an inconsistency here
                if (b.contains(0)) return true;

                // compute rough bounds: a / b  (conservative)
                int[] q = divBounds(a.getMin(), a.getMax(), b.getMin(), b.getMax());
                return intervalsOverlap(r.getMin(), r.getMax(), q[0], q[1]);
            }

            default:
                return true;
        }
    }

    private static int safeAdd(int x, int y) {
        long t = (long) x + y;
        return (t > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (t < Integer.MIN_VALUE) ? Integer.MIN_VALUE : (int) t;
    }

    private static int safeSub(int x, int y) {
        long t = (long) x - y;
        return (t > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (t < Integer.MIN_VALUE) ? Integer.MIN_VALUE : (int) t;
    }

    private static int clampToInt(long v) {
        return (v > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (v < Integer.MIN_VALUE) ? Integer.MIN_VALUE : (int) v;
    }

    private static int[] divBounds(int aMin, int aMax, int bMin, int bMax) {
        int[] vals = new int[]{
                floorDiv(aMin, bMin), floorDiv(aMin, bMax),
                floorDiv(aMax, bMin), floorDiv(aMax, bMax)
        };
        int lo = vals[0], hi = vals[0];
        for (int v : vals) { if (v < lo) lo = v; if (v > hi) hi = v; }
        return new int[]{lo, hi};
    }

    private static int floorDiv(int a, int b) {
        if (b == 0) throw new ArithmeticException("division by zero in bounds");
        double d = Math.floor((double) a / (double) b);
        if (d > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (d < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) d;
    }
}
