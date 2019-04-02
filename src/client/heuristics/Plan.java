package client.heuristics;

import client.*;

import java.util.ArrayList;
import java.util.HashSet;

public class Plan {

    private State state;
    private Plan parent;
    private int time;
    private Action action;

    public Plan(State state, Plan parent, int time, Action action) {
        this.state = state;
        this.parent = parent;
        this.time = time;
        this.action = action;
    }

    private int findBox(Position position) {
        Box[] boxes = this.state.getBoxes();

        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i].getPosition().equals(position)) {
                return i;
            }
        }
        return -1;
    }

    private Plan getNewBoxPlan(int agentID) {
        Agent[] agents = this.state.getAgents();
        Box[] boxes = this.state.getBoxes();
        Agent[] pullAgents = agents.clone();
        pullAgents[agentIndex] = new Agent(agentID, agent.getColor(), newAgentPos);
        int boxIndex = this.findBox(boxPos);
        Box box = boxes[boxIndex];
        Box[] pullBoxes = boxes.clone();
        pullBoxes[boxIndex] = new Box(box.getLetter(), box.getColor(), agentPos);
        State pullState = new State(pullAgents, pullBoxes, goals);
        Action pullAction = new Action(c, new Timestamp[] { agentTimestamp, newAgentTimestamp, boxTimestamp });
        Plan pullPlan = new Plan(pullState, this, this.time + 1, pullAction);
        children.add(pullPlan);
    }

    public ArrayList<Plan> getChildren(int agentID, HashSet<Timestamp> constraints) {

        ArrayList<Plan> children = new ArrayList<>();
        Agent agent = null;
        int agentIndex = -1;
        Box[] boxes = this.state.getBoxes();
        Goal[] goals = this.state.getGoals();

        Agent[] agents = this.state.getAgents();
        for (int i = 0; i < agents.length; i++) {
            if (agents[i].getId() == agentID) {
                agent = agents[i];
                agentIndex = i;
                break;
            }
        }

        Position agentPos = agent.getPosition();
        int agentX = agentPos.getCol();
        int agentY = agentPos.getRow();
        Timestamp agentTimestamp = new Timestamp(this.time + 1, agentPos);

        for (Command c : Command.EVERY) {
            if (c.actionType == Command.Type.NoOp) {
                Action noOpAction = new Action(Command.NoOp, new Timestamp[] { agentTimestamp });
                Plan noOpPlan = new Plan(this.state, this, this.time + 1, noOpAction);
                children.add(noOpPlan);
                continue;
            }

            int newAgentX = agentX + Command.dirToColChange(c.dir1);
            int newAgentY = agentY + Command.dirToRowChange(c.dir1);
            Position newAgentPos = new Position(newAgentX, newAgentY);
            Timestamp newAgentTimestamp = new Timestamp(this.time + 1, newAgentPos);

            if (newAgentX < 0 || newAgentY < 0 || newAgentX > State.getColCount() - 1 || newAgentY > State.getRowCount() - 1) {
                continue;
            }

            if (constraints.contains(agentTimestamp) && constraints.contains(newAgentTimestamp)) {
                continue;
            }

            switch (c.actionType) {
                case Move:
                    if (this.state.isFree(newAgentPos)) {
                        Agent[] moveAgents = agents.clone();
                        moveAgents[agentIndex] = new Agent(agentID, agent.getColor(), newAgentPos);
                        State moveState = new State(moveAgents, boxes, goals);
                        Action moveAction = new Action(c, new Timestamp[] { agentTimestamp, newAgentTimestamp});
                        Plan movePlan = new Plan(moveState, this, this.time + 1, moveAction);
                        children.add(movePlan);
                    }
                    break;
                case Pull:
                    int boxX = agentX + Command.dirToColChange(c.dir2);
                    int boxY = agentY + Command.dirToRowChange(c.dir2);
                    Position boxPos = new Position(boxX, boxY);
                    Timestamp boxTimestamp = new Timestamp(this.time + 1, boxPos);

                    if (!constraints.contains(boxTimestamp) && this.state.boxAt(boxPos)) {
                        Agent[] pullAgents = agents.clone();
                        pullAgents[agentIndex] = new Agent(agentID, agent.getColor(), newAgentPos);
                        int boxIndex = this.findBox(boxPos);
                        Box box = boxes[boxIndex];
                        Box[] pullBoxes = boxes.clone();
                        pullBoxes[boxIndex] = new Box(box.getLetter(), box.getColor(), agentPos);
                        State pullState = new State(pullAgents, pullBoxes, goals);
                        Action pullAction = new Action(c, new Timestamp[] { agentTimestamp, newAgentTimestamp, boxTimestamp });
                        Plan pullPlan = new Plan(pullState, this, this.time + 1, pullAction);
                        children.add(pullPlan);
                    }
                    break;
                case Push:
                    int newBoxX = newAgentX + Command.dirToColChange(c.dir2);
                    int newBoxY = newAgentY + Command.dirToRowChange(c.dir2);
                    Position newBoxPos = new Position(newBoxX, newBoxY);
                    Timestamp newBoxTimestamp = new Timestamp(this.time + 1, newBoxPos);

                    if (newBoxX < 0 || newBoxY < 0 || newBoxX > State.getColCount() - 1 || newBoxY > State.getRowCount() - 1) {
                        continue;
                    }

                    if (this.state.isFree((newBoxPos)) && !constraints.contains(newBoxTimestamp) && this.state.boxAt(newAgentPos)) {
                        Agent[] pushAgents = agents.clone();
                        pushAgents[agentIndex] = new Agent(agentID, agent.getColor(), newAgentPos);
                        int boxIndex = this.findBox(newAgentPos);
                        Box box = boxes[boxIndex];
                        Box[] pushBoxes = boxes.clone();
                        pushBoxes[boxIndex] = new Box(box.getLetter(), box.getColor(), newBoxPos);
                        State pushState = new State(pushAgents, pushBoxes, goals);
                        Action pushAction = new Action(c, new Timestamp[] { agentTimestamp, newAgentTimestamp, newBoxTimestamp });
                        Plan pushPlan = new Plan(pushState, this, this.time + 1, pushAction);
                        children.add(pushPlan);
                    }
                    break;
            }
        }

        return children;
    }
}
