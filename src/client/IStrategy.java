package client;

public interface IStrategy {

    public Command[][] plan(State initialState, IHeuristic heuristic);

}
