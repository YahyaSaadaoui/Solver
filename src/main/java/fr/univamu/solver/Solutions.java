package fr.univamu.solver;

import java.util.*;

public class Solutions {

    public record Assignment(Map<String, Integer> values) {
        @Override
        public String toString() {
            return values.toString();
        }
    }

    private final List<Assignment> allSolutions = new ArrayList<>();
    private boolean display = true;

    public void addSolution(List<Variable> variables) {
        Map<String, Integer> snapshot = new LinkedHashMap<>();

        for (Variable v : variables) {
            if (v.isNamed()) {
                snapshot.put(v.getName(), v.getFixedValue());
            }
        }

        allSolutions.add(new Assignment(snapshot));

        if (display) {
            System.out.println(snapshot);
        }
    }

    public long count() {
        return allSolutions.size();
    }

    public List<Assignment> getAll() {
        return Collections.unmodifiableList(allSolutions);
    }

    public void clear() {
        allSolutions.clear();
    }


    public void setDisplay(boolean display) {
        this.display = display;
    }

    public boolean isDisplay() {
        return display;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Solutions:\n");
        for (Assignment a : allSolutions) {
            sb.append("  ").append(a).append("\n");
        }
        return sb.toString();
    }
}
