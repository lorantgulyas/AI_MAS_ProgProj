package client;

public class PerformanceStats {

    private double memoryUsed; // in MB
    private long messagesSent;
    private long nodesExplored;
    private long nodesGenerated;
    private long solutionLength;
    private double timeSpent; // in seconds

    private String formatString;

    public PerformanceStats(
            double memoryUsed,
            long messagesSent,
            long nodesExplored,
            long nodesGenerated,
            long solutionLength,
            double timeSpent
    ) {
        this.memoryUsed = memoryUsed;
        this.messagesSent = messagesSent;
        this.nodesExplored = nodesExplored;
        this.nodesGenerated = nodesGenerated;
        this.solutionLength = solutionLength;
        this.timeSpent = timeSpent;
        this.formatString = this.makeFormatString();
    }

    private String makeFormatString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Memory used: %f MB\n");
        builder.append("Time spent: %f seconds\n");
        builder.append("Solution length: %d\n");
        builder.append("Messages sent: %d\n");
        builder.append("Nodes explored: %d\n");
        builder.append("Nodes generated: %d");
        return builder.toString();
    }

    public String getMemoryUsed() {
        // do not change this since the performance tool expects a specific format
        return String.format("Memory used: %f MB", this.memoryUsed);
    }

    public String getTimeSpent() {
        // do not change this since the performance tool expects a specific format
        return String.format("Time spent: %f seconds", this.timeSpent);
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

    public String getSolutionLength() {
        // do not change this since the performance tool expects a specific format
        return String.format("Solution length: %d", this.solutionLength);
    }

    @Override
    public String toString() {
        return String.format(
                this.formatString,
                this.memoryUsed,
                this.timeSpent,
                this.solutionLength,
                this.messagesSent,
                this.nodesExplored,
                this.nodesGenerated
        );
    }
}
