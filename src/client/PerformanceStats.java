package client;

public class PerformanceStats {

    private long messagesSent;
    private long nodesExplored;
    private long nodesGenerated;

    private String formatString;

    public PerformanceStats(long messagesSent, long nodesExplored, long nodesGenerated) {
        this.messagesSent = messagesSent;
        this.nodesExplored = nodesExplored;
        this.nodesGenerated = nodesGenerated;
        this.formatString = this.makeFormatString();
    }

    private String makeFormatString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Messages sent: %d\n");
        builder.append("Nodes explored: %d\n");
        builder.append("Nodes generated: %d");
        return builder.toString();
    }

    public long messagesSent() {
        return this.messagesSent;
    }

    public long nodesExplored() {
        return this.nodesExplored;
    }

    public long nodesGenerated() {
        return this.nodesGenerated;
    }

    public String getMemoryUsed(double memoryUsed) {
        // do not change this since the performance tool expects a specific format
        return String.format("Memory used: %f MB", memoryUsed);
    }

    public String getTimeSpent(double timeSpent) {
        // do not change this since the performance tool expects a specific format
        return String.format("Time spent: %f seconds", timeSpent);
    }

    public String getMessagesSent() {
        // do not change this since the performance tool expects a specific format
        return String.format("Messages sent: %d", this.messagesSent);
    }

    public String getNodesExplored() {
        // do not change this since the performance tool expects a specific format
        return String.format("Nodes explored: %d", this.nodesExplored);
    }

    public String getNodesGenerated() {
        // do not change this since the performance tool expects a specific format
        return String.format("Nodes generated: %d", this.nodesGenerated);
    }

    public String getSolutionLength(int solutionLength) {
        // do not change this since the performance tool expects a specific format
        return String.format("Solution length: %d", solutionLength);
    }

    public static double timeSpent(long startTime) {
        return (System.currentTimeMillis() - startTime) / 1000f;
    }

    @Override
    public String toString() {
        return String.format(
                this.formatString,
                this.messagesSent,
                this.nodesExplored,
                this.nodesGenerated
        );
    }
}
