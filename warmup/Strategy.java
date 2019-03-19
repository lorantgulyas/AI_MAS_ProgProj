package searchclient;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Stack;
import java.util.PriorityQueue;

public abstract class Strategy {
    private HashSet<State> explored;
    private final long startTime;

    public Strategy() {
        this.explored = new HashSet<>();
        this.startTime = System.currentTimeMillis();
    }

    public void addToExplored(State n) {
        this.explored.add(n);
    }

    public boolean isExplored(State n) {
        return this.explored.contains(n);
    }

    public int countExplored() {
        return this.explored.size();
    }

    public String searchStatus() {
        return String.format("#Explored: %,6d, #Frontier: %,6d, #Generated: %,6d, Time: %3.2f s \t%s", this.countExplored(), this.countFrontier(), this.countExplored()+this.countFrontier(), this.timeSpent(), Memory.stringRep());
    }

    public float timeSpent() {
        return (System.currentTimeMillis() - this.startTime) / 1000f;
    }

    public abstract State getAndRemoveLeaf();

    public abstract void addToFrontier(State n);

    public abstract boolean inFrontier(State n);

    public abstract int countFrontier();

    public abstract boolean frontierIsEmpty();

    @Override
    public abstract String toString();

    public static class StrategyBFS extends Strategy {
        private ArrayDeque<State> frontier;
        private HashSet<State> frontierSet;

        public StrategyBFS() {
            super();
            frontier = new ArrayDeque<>();
            frontierSet = new HashSet<>();
        }

        @Override
        public State getAndRemoveLeaf() {
            State n = frontier.pollFirst();
            frontierSet.remove(n);
            return n;
        }

        @Override
        public void addToFrontier(State n) {
            frontier.addLast(n);
            frontierSet.add(n);
        }

        @Override
        public int countFrontier() {
            return frontier.size();
        }

        @Override
        public boolean frontierIsEmpty() {
            return frontier.isEmpty();
        }

        @Override
        public boolean inFrontier(State n) {
            return frontierSet.contains(n);
        }

        @Override
        public String toString() {
            return "Breadth-first Search";
        }
    }

    public static class StrategyDFS extends Strategy {
        private Stack<State> frontier;
        private HashSet<State> frontierSet;

        public StrategyDFS() {
            super();
            frontier = new Stack<>();
            frontierSet = new HashSet<>();
        }

        @Override
        public State getAndRemoveLeaf() {
            State n = frontier.pop();
            frontierSet.remove(n);
            return n;
        }

        @Override
        public void addToFrontier(State n) {
            frontier.push(n);
            frontierSet.add(n);
        }

        @Override
        public int countFrontier() {
            return frontier.size();
        }

        @Override
        public boolean frontierIsEmpty() {
            return frontier.empty();
        }

        @Override
        public boolean inFrontier(State n) {
            return frontierSet.contains(n);
        }

        @Override
        public String toString() {
            return "Depth-first Search";
        }
    }

    public static class StrategyBestFirst extends Strategy {
        private Heuristic heuristic;
        private PriorityQueue<State> frontier;
        private HashSet<State> frontierSet;

        public StrategyBestFirst(Heuristic h) {
            super();
            this.heuristic = h;
            frontier = new PriorityQueue<>(100, heuristic);
            frontierSet = new HashSet<>();
        }

        @Override
        public State getAndRemoveLeaf() {
            State n = frontier.poll();
            frontierSet.remove(n);
            return n;
        }

        @Override
        public void addToFrontier(State n) {
            frontier.add(n);
            frontierSet.add(n);
        }

        @Override
        public int countFrontier() {
            return frontier.size();
        }

        @Override
        public boolean frontierIsEmpty() {
            return frontier.isEmpty();
        }

        @Override
        public boolean inFrontier(State n) {
            return frontierSet.contains(n);
        }

        @Override
        public String toString() {
            return "Best-first Search using " + this.heuristic.toString();
        }
    }
}
