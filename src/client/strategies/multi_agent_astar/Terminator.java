package client.strategies.multi_agent_astar;

public class Terminator {

    private boolean killed;
    private boolean noSolutionExists;
    private boolean solutionFound;

    /**
     * Creates a new instance of a class made to inform agents of
     * when to stop searching for a solution. In other words it
     * informs the agents of when to terminate.
     */
    public Terminator() {
        this.noSolutionExists = false;
        this.solutionFound = false;
        this.killed = false;
    }

    public boolean isAlive() {
        return !this.noSolutionExists && !this.solutionFound && !this.killed;
    }

    public void foundNoSolution() {
        this.noSolutionExists = true;
    }

    public void foundSolution() {
        this.solutionFound = true;
    }

    public void kill() {
        this.killed = true;
    }
}
