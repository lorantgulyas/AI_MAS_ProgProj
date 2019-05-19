package subgoaler;

import java.io.PipedOutputStream;
import java.lang.reflect.Array;
import java.util.*;

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
//        for (int i = 0; i < 1; i++) {
//            if (i == 59) {
//                System.err.println("kek");
//            }
            State.setGoals(Arrays.copyOfRange(allGoals, 0, i + 1));
            System.err.println("--- task: " + i + ", goal: " + allGoals[i]);

            ArrayList<Command> subCmds;

            try {
                subCmds = plan(subresult);
            } catch (BlockedException e) {
                System.err.println("freeing path for " + allGoals[i]);
                ArrayList<Position> path = ff.findPath(allGoals[i].getPosition(),
                        subresult.getBoxes()[i].getPosition());
                // path does not contain goal itself we have to add it manually
                path.add(allGoals[i].getPosition());
                subCmds = freePath(subresult, path);
                try {
                    subCmds.addAll(plan(subresult));
                } catch (BlockedException ex) {
                    System.err.println("trying again: " + allGoals[i]);
                    System.err.println("we failed big times");
                }
            }

            cmds.addAll(subCmds);

            //            for (Command cmd : subCmds) {
//                System.err.println(cmd);
//            }
        }
        return cmds;
    }

    public ArrayList<Command> freePath(State init, ArrayList<Position> path) {
        Comparator<State> comp = (s1, s2) ->
                s1.g() + ff.hPath(s1, path) - s2.g() - ff.hPath(s2, path);
        AStar strategy = new AStar(comp);
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

            // isGoalState
            if (ff.hPath(leafState, path) == 0) {
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

    public ArrayList<Command> plan(State init) throws BlockedException {
        AStar strategy = new AStar(ff);
        strategy.addToFrontier(init);

        int iterations = 0;
        while (true) {
            if (iterations % 10000 == 0) {
                System.err.println("i: " + iterations + ", " + strategy.searchStatus());
                if (iterations == 40000) {
                    throw new BlockedException();
                }
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
        private PriorityQueue<State> frontier;
        private HashSet<State> frontierSet;
        private HashSet<State> explored;
        private final long startTime;

        public AStar(Comparator<State> comp) {
            frontier = new PriorityQueue<>(100, comp);
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

    class BlockedException extends Exception { }
}
