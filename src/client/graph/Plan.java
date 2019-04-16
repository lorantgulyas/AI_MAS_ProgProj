package client.graph;

import client.state.*;

import java.util.*;

public class Plan {

    private Timestamp[] currentConstraints;
    private HashSet<Timestamp> constraints;
    private State state;
    private Plan parent;
    private int time;
    private Action action;
    private int _hash;

    public Plan(State initialState, HashSet<Timestamp> constraints) {
        this.constraints = constraints;
        this.state = initialState;
        this.parent = null;
        this.time = 0;
        this.action = null;
        this.currentConstraints = this.getCurrentConstraints();
        this._hash = this.computeHashCode();
    }

    public Plan(State state, Plan parent, int time, Action action, HashSet<Timestamp> constraints) {
        this.constraints = constraints;
        this.state = state;
        this.parent = parent;
        this.time = time;
        this.action = action;
        this.currentConstraints = this.getCurrentConstraints();
        this._hash = this.computeHashCode();
    }

    private Timestamp[] getCurrentConstraints() {
        ArrayList<Timestamp> currentConstraints = new ArrayList<>();
        Iterator<Timestamp> iterator = this.constraints.iterator();
        while (iterator.hasNext()) {
            Timestamp timestamp = iterator.next();
            if (timestamp.getTime() == this.time) {
                currentConstraints.add(timestamp);
            }
        }
        return currentConstraints.toArray(new Timestamp[0]);
    }

    private Box findBox(Position position) {
        Box[] boxes = this.state.getBoxes();
        int index = this.findBoxIndex(position);
        return boxes[index];
    }

    private int findBoxIndex(Position position) {
        Box[] boxes = this.state.getBoxes();
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i].getPosition().equals(position)) {
                return i;
            }
        }
        return -1;
    }

    private Action getBoxAction(Command command, Timestamp agentTimestamp, Timestamp newAgentTimestamp, Timestamp boxTimestamp, Timestamp newBoxTimestamp) {
        Timestamp[] timestamps = new Timestamp[] { agentTimestamp, boxTimestamp, newBoxTimestamp, newAgentTimestamp };
        return new Action(command, timestamps);
    }

    private State getBoxState(int agentID, int agentIndex, Command command, Timestamp agentTimestamp, Timestamp newAgentTimestamp, Timestamp boxTimestamp, Timestamp newBoxTimestamp) {
        Agent[] agents = this.state.getAgents();
        Box[] boxes = this.state.getBoxes();

        Position boxPos = boxTimestamp.getPosition();
        Position newBoxPos = newBoxTimestamp.getPosition();
        Position newAgentPos = newAgentTimestamp.getPosition();

        Agent agent = agents[agentIndex];
        Agent[] boxAgents = agents.clone();
        boxAgents[agentIndex] = new Agent(agentID, agent.getColor(), newAgentPos);

        int boxIndex = this.findBoxIndex(boxPos);
        Box box = boxes[boxIndex];
        Box[] actionBoxes = boxes.clone();
        actionBoxes[boxIndex] = new Box(box.getLetter(), box.getColor(), newBoxPos);

        State boxState = new State(boxAgents, actionBoxes , this.state.getGoals());
        return boxState;
    }

    private Position findNewAgentPosition(int agentX, int agentY, Command c) {
        int newAgentX = agentX + Command.dirToColChange(c.dir1);
        int newAgentY = agentY + Command.dirToRowChange(c.dir1);
        Position newAgentPos = new Position(newAgentX, newAgentY);
        return newAgentPos;
    }

    public Action getAction() {
        return action;
    }

    public int g() {
        return this.time;
    }

    public State getState() {
        return state;
    }

    public boolean isInitialState() {
        return this.parent == null;
    }

    public Action[] extract() {
        Plan plan = this;
        ArrayList<Action> actions = new ArrayList<>();
        while (!plan.isInitialState()) {
            actions.add(plan.getAction());
            plan = plan.parent;
        }
        Collections.reverse(actions);
        return actions.toArray(new Action[0]);
    }

    // generates all the possible actions an agent can perform in the current state
    public ArrayList<Action> getChildrenActions(int agentID) {
        ArrayList<Action> children = new ArrayList<>();

        Agent[] agents = this.state.getAgents();
        Agent agent = agents[agentID];

        Position agentPos = agent.getPosition();
        int agentX = agentPos.getCol();
        int agentY = agentPos.getRow();
        Timestamp agentTimestamp = new Timestamp(this.time + 1, agentPos);

        for (Command c : Command.EVERY) {
            if (c.actionType == Command.Type.NoOp) {
                if (!constraints.contains(agentTimestamp)) {
                    Action noOpAction = new Action(Command.NoOp, new Timestamp[] { agentTimestamp });
                    children.add(noOpAction);
                }
                continue;
            }

            Position newAgentPos = this.findNewAgentPosition(agentX, agentY, c);
            Timestamp newAgentTimestamp = new Timestamp(this.time + 1, newAgentPos);

            if (constraints.contains(agentTimestamp) || constraints.contains(newAgentTimestamp)) {
                continue;
            }

            switch (c.actionType) {
                case Move:
                    if (this.state.isFree(newAgentPos)) {
                        Action moveAction = new Action(c, new Timestamp[] { agentTimestamp, newAgentTimestamp});
                        children.add(moveAction);
                    }
                    break;
                case Pull:
                    int boxX = agentX + Command.dirToColChange(c.dir2);
                    int boxY = agentY + Command.dirToRowChange(c.dir2);
                    Position boxPos = new Position(boxX, boxY);
                    Timestamp boxTimestamp = new Timestamp(this.time + 1, boxPos);

                    if (!constraints.contains(boxTimestamp)
                            && this.state.boxAt(boxPos)
                            && this.state.isFree(newAgentPos)) {
                        Box pullBox = this.findBox(boxPos);
                        if (pullBox.getColor() == agent.getColor()) {
                            Action pullAction= this.getBoxAction(c, agentTimestamp, newAgentTimestamp, boxTimestamp, agentTimestamp);
                            children.add(pullAction);
                        }
                    }
                    break;
                case Push:
                    int newBoxX = newAgentPos.getCol() + Command.dirToColChange(c.dir2);
                    int newBoxY = newAgentPos.getRow() + Command.dirToRowChange(c.dir2);
                    Position newBoxPos = new Position(newBoxX, newBoxY);
                    Timestamp newBoxTimestamp = new Timestamp(this.time + 1, newBoxPos);

                    if (newBoxX < 0 || newBoxY < 0 || newBoxX > State.getColCount() - 1 || newBoxY > State.getRowCount() - 1) {
                        break;
                    }

                    if (this.state.isFree(newBoxPos)
                            && !constraints.contains(newBoxTimestamp)
                            && this.state.boxAt(newAgentPos)) {
                        Box pushBox = this.findBox(newAgentPos);
                        if (pushBox.getColor() == agent.getColor()) {
                            Action pushAction= this.getBoxAction(c, agentTimestamp, newAgentTimestamp, newAgentTimestamp, newBoxTimestamp);
                            children.add(pushAction);
                        }
                    }
                    break;
            }
        }

        Collections.shuffle(children);
        return children;
    }

    // generates all the possible child states an agent can reach from the current state
    public ArrayList<Plan> getChildren(int agentID) {
        Position newAgentPos;
        Timestamp newAgentTimestamp;

        ArrayList<Plan> children = new ArrayList<>();
        ArrayList<Action> actions = this.getChildrenActions(agentID);

        Agent[] agents = this.state.getAgents();
        Box[] boxes = this.state.getBoxes();
        Goal[] goals = this.state.getGoals();

        int agentIndex = agentID;
        Agent agent = agents[agentIndex];

        Position agentPos = agent.getPosition();
        int agentX = agentPos.getCol();
        int agentY = agentPos.getRow();
        Timestamp agentTimestamp = new Timestamp(this.time + 1, agentPos);

        for (Action action : actions) {
            Command c = action.getCommand();

            switch (c.actionType) {
                case NoOp:
                    Plan noOpPlan = new Plan(this.state, this, this.time + 1, action, this.constraints);
                    children.add(noOpPlan);
                    break;
                case Move:
                    newAgentPos = this.findNewAgentPosition(agentX, agentY, c);
                    Agent[] moveAgents = agents.clone();
                    moveAgents[agentIndex] = new Agent(agentID, agent.getColor(), newAgentPos);
                    State moveState = new State(moveAgents, boxes, goals);
                    Plan movePlan = new Plan(moveState, this, this.time + 1, action, this.constraints);
                    children.add(movePlan);
                    break;
                case Pull:
                    newAgentPos = this.findNewAgentPosition(agentX, agentY, c);
                    newAgentTimestamp = new Timestamp(this.time + 1, newAgentPos);
                    int boxX = agentX + Command.dirToColChange(c.dir2);
                    int boxY = agentY + Command.dirToRowChange(c.dir2);
                    Position boxPos = new Position(boxX, boxY);
                    Timestamp boxTimestamp = new Timestamp(this.time + 1, boxPos);

                    State pullState = this.getBoxState(agentID, agentIndex, c, agentTimestamp, newAgentTimestamp, boxTimestamp, agentTimestamp);
                    Plan pullPlan = new Plan(pullState, this, this.time + 1, action, this.constraints);
                    children.add(pullPlan);
                    break;
                case Push:
                    newAgentPos = this.findNewAgentPosition(agentX, agentY, c);
                    newAgentTimestamp = new Timestamp(this.time + 1, newAgentPos);
                    int newBoxX = newAgentPos.getCol() + Command.dirToColChange(c.dir2);
                    int newBoxY = newAgentPos.getRow() + Command.dirToRowChange(c.dir2);
                    Position newBoxPos = new Position(newBoxX, newBoxY);
                    Timestamp newBoxTimestamp = new Timestamp(this.time + 1, newBoxPos);

                    State pushState = this.getBoxState(agentID, agentIndex, c, agentTimestamp, newAgentTimestamp, newAgentTimestamp, newBoxTimestamp);
                    Plan pushPlan = new Plan(pushState, this, this.time + 1, action, this.constraints);
                    children.add(pushPlan);
                    break;
            }
        }

        Collections.shuffle(children);
        return children;
    }

    private int computeHashCode() {
        int timestampsHash = 0;
        for (Timestamp timestamp : this.currentConstraints) {
            timestampsHash += timestamp.hashCode();
        }
        // plus one since there might not be any constraints
        return (timestampsHash + 1) * this.state.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        Plan other = (Plan) obj;
        return Arrays.deepEquals(this.currentConstraints, other.currentConstraints)
                && other.getState().equals(this.state);
    }

    @Override
    public int hashCode() {
        return this._hash;
    }
}
