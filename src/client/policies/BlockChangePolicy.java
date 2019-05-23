package client.policies;

import client.graph.Plan;
import client.path.AllObjectsAStar;
import client.path.Node;
import client.state.*;

import java.util.ArrayList;
import java.util.HashMap;

public class BlockChangePolicy extends AbstractGoalChanged {

    private BroadcastPolicy broadcast;
    private AllObjectsAStar planner;
    private HashMap<Goal, Integer> goalBoxMap;

    public BlockChangePolicy(State initialState, AllObjectsAStar planner, HashMap<Goal, Integer> goalBoxMap) {
        super(initialState);
        this.broadcast = new BroadcastPolicy(initialState);
        this.planner = planner;
        this.goalBoxMap = goalBoxMap;
    }

    @Override
    public Iterable<Integer> receivers(Plan node, int senderID) {
        if (this.someGoalHasChanged(node, senderID))
            return this.broadcast.receivers(node, senderID);

        Agent[] agents = node.getState().getAgents();
        ArrayList<Integer> receivers = new ArrayList<>();
        for (Agent agent : agents) {
            int agentID = agent.getId();
            if (agentID != senderID && this.someBlockHasChanged(node, agentID)) {
                receivers.add(agentID);
            }
        }
        return receivers;
    }

    @Override
    public String toString() {
        return "block-change";
    }

    private boolean someBlockHasChanged(Plan node, int agentID) {
        Plan parent = node.getParent();
        if (parent == null)
            return false;

        State previous = parent.getState();
        State current = node.getState();
        Agent[] currentAgents = current.getAgents();
        Agent[] previousAgents = previous.getAgents();
        Agent currentAgent = currentAgents[agentID];
        Agent previousAgent = previousAgents[agentID];
        Color color = currentAgent.getColor();
        Box[] previousBoxes = previous.getBoxes();
        Box[] currentBoxes = current.getBoxes();

        // check if one of the box goals has been blocked/unblocked
        Goal[] goals = current.getLevel().getAgentGoals(agentID);
        for (Goal goal : goals) {
            int boxID = this.goalBoxMap.get(goal);
            Box previousBox = previousBoxes[boxID];
            Box currentBox = currentBoxes[boxID];
            Node previousAgent2Box = this.planner.plan(previous, previousAgent.getPosition(), previousBox.getPosition(), color);
            Node currentAgent2Box = this.planner.plan(current, currentAgent.getPosition(), currentBox.getPosition(), color);
            boolean preva2bBlocked = previousAgent2Box == null;
            boolean curra2bBlocked = currentAgent2Box == null;
            if ((preva2bBlocked && !curra2bBlocked) || (!preva2bBlocked && curra2bBlocked))
                return true;

            Node previousBox2Goal = this.planner.plan(previous, previousBox.getPosition(), goal.getPosition());
            Node currentBox2Goal = this.planner.plan(current, currentBox.getPosition(), goal.getPosition());
            boolean prevb2gBlocked = previousBox2Goal == null;
            boolean currb2gBlocked = currentBox2Goal == null;
            if ((prevb2gBlocked && !currb2gBlocked) || (!prevb2gBlocked && currb2gBlocked))
                return true;
        }

        // check if one of the agent end positions has been blocked/unblocked
        AgentGoal[] agentEndPositions = current.getLevel().getAgentEndPositions();
        for (AgentGoal agentEndPosition : agentEndPositions) {
            if (agentEndPosition.getAgentID() == agentID) {
                Position end = agentEndPosition.getPosition();
                Node previousAgent2End = this.planner.plan(previous, previousAgent.getPosition(), end, color);
                Node currentAgent2End = this.planner.plan(current, currentAgent.getPosition(), end, color);
                boolean preva2eBlocked = previousAgent2End == null;
                boolean curra2eBlocked = currentAgent2End == null;
                if ((preva2eBlocked && !curra2eBlocked) || (!preva2eBlocked && curra2eBlocked))
                    return true;
            }
        }

        return false;
    }

}
