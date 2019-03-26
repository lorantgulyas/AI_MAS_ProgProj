package client.definitions;

import client.Agent;
import client.Box;
import client.Goal;
import client.Position;

public abstract class AState {

    public abstract boolean isFree(Position position);

    public abstract Agent[] getAgents();

    public abstract Box[] getBoxes();

    public abstract Goal[] getGoals();

}
