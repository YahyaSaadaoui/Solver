package fr.univamu.solver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Backup {

    private final Map<Variable, int[]> savedDomains = new HashMap<>();
    public Backup(List<Variable> variables) {
        for (Variable v : variables) {
            savedDomains.put(v, new int[]{v.getMin(), v.getMax()});
        }
    }
    public void restore() {
        for (Map.Entry<Variable, int[]> entry : savedDomains.entrySet()) {
            int[] bounds = entry.getValue();
            entry.getKey().init(bounds[0], bounds[1]);
        }
    }
}
