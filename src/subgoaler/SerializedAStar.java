package subgoaler;

import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;

public class SerializedAStar {
    Floodfill ff;
    private State subresult;

    public SerializedAStar(Floodfill ff) {
        this.ff = ff;
    }

    public ArrayList<Command> serializedPlan(State init) {
        Goal[] allGoals = State.getGoals();
        ArrayList<Command> cmds = new ArrayList<>();
        subresult = init;
        for (int i = 0; i < allGoals.length; i++) {
//        for (int i = 0; i < 7; i++) {
//            if (i == 59) {
//                System.err.println("kek");
//            }
            State.setGoals(Arrays.copyOfRange(allGoals, 0, i + 1));
            System.err.println("--- task: " + i + ", goal: " + allGoals[i]);
            ArrayList<Command> subCmds = plan(subresult);
            cmds.addAll(subCmds);
//            for (Command cmd : subCmds) {
//                System.err.println(cmd);
//            }
        }
        return cmds;
    }

    public ArrayList<Command> plan(State init) {
        AStar strategy = new AStar(ff);
        strategy.addToFrontier(init);

        int iterations = 0;
        while (true) {
            if (iterations % 10000 == 0) {
                System.err.println("i: " + iterations + ", " + strategy.searchStatus());
            }

            if (strategy.frontierIsEmpty()) {
                return null;
            }

            State leafState = strategy.getAndRemoveLeaf();

            // debug stuff starts
//            int g = leafState.g();
//            int h = ff.h(leafState);
//            Position agentPos = leafState.getAgents()[0].getPosition();
//            Position boxPos = leafState.getBoxes()[State.getGoals().length - 1].getPosition();
            // debug stuff ends

            if (leafState.isGoalState()) {
                ArrayList<Command> res = leafState.extractPlan();
                subresult = leafState;
                subresult.parent = null;
                subresult.cmd = null;
                return res;
            }

            strategy.addToExplored(leafState);
            for (State n : leafState.getExpandedStates()) { // The list of expanded states is shuffled randomly; see State.java.
                if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
                    strategy.addToFrontier(n);
                }
            }
            iterations++;
        }
    }

    public class AStar {
        private Floodfill ff;
        private PriorityQueue<State> frontier;
        private HashSet<State> frontierSet;
        private HashSet<State> explored;
        private final long startTime;

        public AStar(Floodfill ff) {
            this.ff = ff;
            frontier = new PriorityQueue<>(100, ff);
            frontierSet = new HashSet<>();
            explored = new HashSet<>();
            this.startTime = System.currentTimeMillis();
        }

        public int countExplored() {
            return this.explored.size();
        }

        public float timeSpent() {
            return (System.currentTimeMillis() - this.startTime) / 1000f;
        }

        public String searchStatus() {
            return String.format("#Explored: %,6d, #Frontier: %,6d, #Generated: %,6d, Time: %3.2f s \t%s", this.countExplored(), this.countFrontier(), this.countExplored()+this.countFrontier(), this.timeSpent(), Memory.stringRep());
        }

        public State getAndRemoveLeaf() {
            State n = frontier.poll();
            frontierSet.remove(n);
            return n;
        }

        public void addToFrontier(State n) {
            frontier.add(n);
            frontierSet.add(n);
        }

        public boolean isExplored(State n) {
            return this.explored.contains(n);
        }

        public int countFrontier() {
            return frontier.size();
        }

        public void addToExplored(State n) {
            this.explored.add(n);
        }

        public boolean frontierIsEmpty() {
            return frontier.isEmpty();
        }

        public boolean inFrontier(State n) {
            return frontierSet.contains(n);
        }
    }
}

