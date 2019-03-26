package client.definitions;

import client.*;

public abstract class AState {

    public abstract boolean isFree(Position position);

    public abstract Agent[] getAgents();

    public abstract Box[] getBoxes();

    public abstract Goal[] getGoals();

    public abstract Command[] extractPlan();

    public abstract boolean isGoalState();

    public abstract AState[] getExpandedStates();

}
