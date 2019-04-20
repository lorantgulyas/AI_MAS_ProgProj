package client.graph;

import client.state.*;

public class StateGenerator {

    /**
     * Generates the state resulting from executing an action.
     * Assumes that the action is permitted.
     *
     * @param state State to execute the action in.
     * @param action Action to be executed.
     * @return The generated state.
     */
    public static State generate(State state, Action action) {
        return StateGenerator.applyAction(state, action.getAgentID(), action.getCommand());
    }

    /**
     * Generates the state resulting from an agent executing a command.
     * Assumes that the action is permitted.
     *
     * @param state State to execute the command in.
     * @param agentID The agent to execute the command for.
     * @param command The command to be executed.
     * @return The generated state.
     */
    public static State generate(State state, int agentID, Command command) {
        return StateGenerator.applyAction(state, agentID, command);
    }

    private static State applyAction(State s, int agentID, Command c) {
        Agent[] agents = s.getAgents();
        Box[] boxes = s.getBoxes();
        Agent agent = agents[agentID];
        Position agentPos = agent.getPosition();
        switch (c.actionType) {
            case NoOp:
                return s;
            case Move:
                Position newAgentPos = agentPos.go(c.dir1);
                Agent[] moveAgents = agents.clone();
                moveAgents[agentID] = new Agent(agentID, agent.getColor(), newAgentPos);
                return new State(s.getLevel(), moveAgents, boxes);
            case Pull:
                newAgentPos = agentPos.go(c.dir1);
                Position boxPos = agentPos.go(c.dir2);
                return StateGenerator.getBoxState(s, agent, newAgentPos, boxPos, agentPos);
            case Push:
                newAgentPos = agentPos.go(c.dir1);
                Position newBoxPos = newAgentPos.go(c.dir2);
                return StateGenerator.getBoxState(s, agent, newAgentPos, newAgentPos, newBoxPos);
            default:
                // this case should not be reached
                return null;
        }
    }

    private static State getBoxState(State state, Agent agent, Position newAgentPos, Position boxPos, Position newBoxPos) {
        Agent[] agents = state.getAgents();
        Box[] boxes = state.getBoxes();

        Agent[] boxAgents = agents.clone();
        boxAgents[agent.getId()] = new Agent(agent.getId(), agent.getColor(), newAgentPos);

        Box box = state.getBoxAt(boxPos);
        int boxID = box.getId();
        Box[] actionBoxes = boxes.clone();
        actionBoxes[boxID] = new Box(boxID, box.getLetter(), box.getColor(), newBoxPos);

        return new State(state.getLevel(), boxAgents, actionBoxes);
    }

    /**
     * Generates the state resulting from executing multiple consecutive actions.
     * Assumes that the actions are permitted in the given order.
     *
     * @param state State to execute first action in.
     * @param actions Actions to be executed.
     * @return The generated state.
     */
    public static State generate(State state, Iterable<Action> actions) {
        Level level = state.getLevel();
        Agent[] agents = state.getAgents().clone();
        Box[] boxes = state.getBoxes().clone();
        for (Action action : actions) {
            int agentID = action.getAgentID();
            Agent agent = agents[agentID];
            Position agentPos = agent.getPosition();
            Command command = action.getCommand();
            switch (command.actionType) {
                case NoOp:
                    break;
                case Move:
                    Position newAgentPos = agentPos.go(command.dir1);
                    agents[agentID] = new Agent(agentID, agent.getColor(), newAgentPos);
                    break;
                case Pull:
                    newAgentPos = agentPos.go(command.dir1);
                    Position pullBoxPos = agentPos.go(command.dir2);
                    Position newPullBoxPos = agentPos;
                    agents[agentID] = new Agent(agentID, agent.getColor(), newAgentPos);
                    Box pullBox = StateGenerator.findBox(boxes, pullBoxPos);
                    int pullBoxID = pullBox.getId();
                    boxes[pullBoxID] = new Box(pullBoxID, pullBox.getLetter(), pullBox.getColor(), newPullBoxPos);
                    break;
                case Push:
                    newAgentPos = agentPos.go(command.dir1);
                    Position pushBoxPos = newAgentPos;
                    Position newPushBoxPos = newAgentPos.go(command.dir2);
                    agents[agentID] = new Agent(agentID, agent.getColor(), newAgentPos);
                    Box pushBox = StateGenerator.findBox(boxes, pushBoxPos);
                    int pushBoxID = pushBox.getId();
                    boxes[pushBoxID] = new Box(pushBoxID, pushBox.getLetter(), pushBox.getColor(), newPushBoxPos);
                    break;
            }
        }
        return new State(level, agents, boxes);
    }

    private static Box findBox(Box[] boxes, Position position) {
        for (Box box : boxes) {
            if (box.getPosition().equals(position)) {
                return box;
            }
        }
        return null;
    }
}
