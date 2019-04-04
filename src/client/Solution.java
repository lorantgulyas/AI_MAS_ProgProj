package client;

import client.graph.Command;

public class Solution {

    private Command[][] plan;
    private PerformanceStats stats;

    public Solution(Command[][] plan, PerformanceStats stats) {
        this.plan = plan;
        this.stats = stats;
    }

    public Command[][] getPlan() {
        return plan;
    }

    public PerformanceStats getStats() {
        return stats;
    }
}
