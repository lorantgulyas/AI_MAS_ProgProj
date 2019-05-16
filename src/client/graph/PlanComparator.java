package client.graph;

import java.util.Comparator;

public class PlanComparator implements Comparator<AbstractPlan> {

    @Override
    public int compare(AbstractPlan p1, AbstractPlan p2) {
        return p1.f() - p2.f();
    }

}
