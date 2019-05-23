package client.heuristics.unblocker;

import client.definitions.ADistance;
import client.definitions.AHeuristic;
import client.path.AllObjectsAStar;
import client.path.WallOnlyAStar;
import client.state.*;

import java.util.*;

public class Unblocker extends AHeuristic {

    private BlockedAgentEndPositionFinder[] agentEndPositionBlockFinders;
    private BlockedFinder[] blockFinders;
    private ADistance measurer;
    private Level level;
    private AllObjectsAStar objectPlanner;
    private WallOnlyAStar wallPlanner;

    private HashMap<Goal, Integer> goalBoxMap;

    public Unblocker(
            State initialState,
            ADistance measurer,
            AllObjectsAStar objectPlanner,
            WallOnlyAStar wallPlanner,
            HashMap<Goal, Integer> goalBoxMap
    ) {
        super(initialState);
        this.measurer = measurer;
        this.objectPlanner = objectPlanner;
        this.wallPlanner = wallPlanner;
        this.level = initialState.getLevel();
        this.goalBoxMap = goalBoxMap;
        this.agentEndPositionBlockFinders = this.getAgentEndPositionFinders(initialState);
        this.blockFinders = this.getBlockFinders(initialState);
    }

    public int h(State state) {
        int hBlocks = this.sumOfBlocks(state);
        int hGoals = this.sumOfGoals(state);
        int hAgentEndPositions = this.sumOfAgentEndPositions(state);

        /*
        // DEBUG
        HashSet<Block> blocks = this.getBlocks(state);
        Agent[] agents = state.getAgents();
        ArrayList<String> as = new ArrayList<>();
        for (Agent agent : agents) {
            as.add("(" + agent.getId() + "," + agent.getPosition());
        }
        Box[] boxes = state.getBoxes();
        ArrayList<String> bos = new ArrayList<>();
        for (Box box : boxes) {
            bos.add("(" + box.getId() + "," + box.getLetter() + "," + box.getPosition());
        }
        ArrayList<String> bs = new ArrayList<>();
        for (Block block : blocks) {
            bs.add(block.toString(this.measurer));
        }
        ArrayList<String> gs = new ArrayList<>();
        for (Goal goal : this.level.getGoals()) {
            gs.add("(" + this.goalBoxMap.get(goal) + "," + goal.getLetter() + "," + goal.getPosition() + ")");
        }
        gs.sort(Comparator.naturalOrder());
        System.err.println("Agents: " + as);
        System.err.println("Boxes: " + bos);
        System.err.println("Goals: " + gs);
        System.err.println("Blocks: " + bs);
        System.err.println("h_blocks = " + hBlocks + ", h_goals = " + hGoals);
        System.err.println("---------");
        */

        return hBlocks + hGoals + hAgentEndPositions;
    }

    @Override
    public String toString() {
        return "unblocker";
    }

    private BlockedAgentEndPositionFinder[] getAgentEndPositionFinders(State initalState) {
        AgentGoal[] agentEndPositions = initalState.getLevel().getAgentEndPositions();
        BlockedAgentEndPositionFinder[] agentEndPositionBlockFinders = new BlockedAgentEndPositionFinder[agentEndPositions.length];
        for (int i = 0; i < agentEndPositions.length; i++) {
            AgentGoal agentEndPosition = agentEndPositions[i];
            agentEndPositionBlockFinders[i] =  new BlockedAgentEndPositionFinder(
                    this.measurer,
                    agentEndPosition,
                    this.objectPlanner,
                    this.wallPlanner
            );
        }
        return agentEndPositionBlockFinders;
    }

    private BlockedFinder[] getBlockFinders(State initialState) {
        Goal[] goals = initialState.getLevel().getGoals();
        BlockedFinder[] blockFinders = new BlockedFinder[goals.length];
        for (int i = 0; i < goals.length; i++) {
            Goal goal = goals[i];
            int boxID = this.goalBoxMap.get(goal);
            blockFinders[i] =  new BlockedFinder(
                    initialState,
                    this.measurer,
                    goal,
                    boxID,
                    this.objectPlanner,
                    this.wallPlanner
            );
        }
        return blockFinders;
    }

    private HashSet<Block> getBlocks(State state) {
        HashSet<Block> blocks = new HashSet<>();
        for (BlockedFinder blockFinder : this.blockFinders) {
            HashSet<Block> goalBlocks = blockFinder.getBlocks(state);
            blocks.addAll(goalBlocks);
        }
        for (BlockedAgentEndPositionFinder agentEndPositionBlockFinder : this.agentEndPositionBlockFinders) {
            HashSet<Block> agentEndPositionBlocks = agentEndPositionBlockFinder.getBlocks(state);
            blocks.addAll(agentEndPositionBlocks);
        }
        return blocks;
    }

    private int sumOfBlocks(State state) {
        HashSet<Block> blocks = this.getBlocks(state);
        int h = 0;
        for (Block block : blocks) {
            h += block.h(state, this.measurer);
        }
        return h;
    }

    private int sumOfGoals(State state) {
        int h = 0;
        Box[] boxes = state.getBoxes();
        Goal[] goals = this.level.getGoals();
        for (Goal goal : goals) {
            int boxID = this.goalBoxMap.get(goal);
            Box box = boxes[boxID];
            if (!box.getPosition().equals(goal.getPosition())) {
                // important to multiply to make it more "profitable" to reach a goal than to be close to a box
                h += 3 * this.measurer.distance(state, box.getPosition(), goal.getPosition());
                Agent agent = this.findClosestAgentToBox(state, box);
                if (agent != null) {
                    // important to multiply to make it more "profitable" to reach a goal
                    // than to be close to an end position
                    h += 2 * this.measurer.distance(state, agent.getPosition(), box.getPosition());
                }
            }
        }
        return h;
    }

    private int sumOfAgentEndPositions(State state) {
        int h = 0;
        Agent[] agents = state.getAgents();
        for (AgentGoal agentEndPosition : this.level.getAgentEndPositions()) {
            Agent agent = agents[agentEndPosition.getAgentID()];
            h += this.measurer.distance(state, agent.getPosition(), agentEndPosition.getPosition());
        }
        return h;
    }

    private Agent findClosestAgentToBox(State state, Box box) {
        Agent[] agents = state.getAgents();
        Agent closestAgent = null;
        int minDistance = Integer.MAX_VALUE;
        for (Agent agent : agents) {
            if (agent.getColor() == box.getColor()) {
                int distance = this.measurer.distance(state, agent.getPosition(), box.getPosition());
                if (closestAgent == null || distance < minDistance) {
                    closestAgent = agent;
                    minDistance = distance;
                }
            }
        }
        return closestAgent;
    }

}
