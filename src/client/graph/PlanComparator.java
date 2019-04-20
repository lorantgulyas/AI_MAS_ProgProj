package client.graph;

import java.util.Comparator;

public class PlanComparator implements Comparator<Plan> {

    @Override
    public int compare(Plan p1, Plan p2) {
        return p1.f() - p2.f();
    }

}
