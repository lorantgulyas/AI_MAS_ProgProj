package client.policies;

import client.definitions.AMessagePolicy;
import client.graph.Plan;
import client.state.*;

/**
 * This policy is not supposed to be instantiated. Instead it serves
 * as a super class for other policies to extend.
 */
abstract class AbstractGoalChanged extends AMessagePolicy {

    AbstractGoalChanged(State initialState) {
        super(initialState);
    }

    boolean someGoalHasChanged(Plan node, int sender) {
        Plan parent = node.getParent();
        if (parent == null)
            return false;
        State parentState = parent.getState();
        State state = node.getState();
        Level level = state.getLevel();

        // check if a box goal has changed
        Goal[] agentGooals = level.getAgentGoals(sender);
        for (Goal agentGoal : agentGooals) {
            Position goalPosition = agentGoal.getPosition();
            char goalLetter = agentGoal.getLetter();
            Box box = state.getBoxAt(goalPosition);
            Box parentBox = parentState.getBoxAt(goalPosition);
            boolean reachedGoal = box != null && box.getLetter() == goalLetter;
            boolean parentReachedGoal = parentBox != null && parentBox.getLetter() == goalLetter;
            if ((reachedGoal && !parentReachedGoal) || (!reachedGoal && parentReachedGoal)) {
                return true;
            }
        }

        // check if an agent end position has changed
        AgentGoal[] agentEndPositions = level.getAgentEndPositions();
        for (AgentGoal agentEndPosition : agentEndPositions) {
            Position position = agentEndPosition.getPosition();
            int agentID = agentEndPosition.getAgentID();
            Agent agent = state.getAgentAt(position);
            Agent parentAgent = parentState.getAgentAt(position);
            boolean reached = agent != null && agent.getId() == agentID;
            boolean parentReached = parentAgent != null && parentAgent.getId() == agentID;
            if ((reached && !parentReached) || (!reached && parentReached)) {
                return true;
            }
        }

        return false;
    }

}
