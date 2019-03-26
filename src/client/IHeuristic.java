package client;

public interface IHeuristic {

    public int h(State state);

    public void preprocess(State initialState);
}
