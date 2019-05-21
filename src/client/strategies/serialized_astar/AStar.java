//package client.strategies.serialized_astar;
//
//import client.Memory;
//import client.graph.Plan;
//
//import java.util.Comparator;
//import java.util.HashSet;
//import java.util.PriorityQueue;
//
//public class AStar {
//    private PriorityQueue<Plan> frontier;
//    private HashSet<Plan> frontierSet;
//    private HashSet<Plan> explored;
//    private final long startTime;
//
//    public AStar(Comparator<Plan> comp) {
//        this.frontier = new PriorityQueue<>(100, comp);
//        this.frontierSet = new HashSet<>();
//        this.explored = new HashSet<>();
//        this.startTime = System.currentTimeMillis();
//    }
//
//    public int countExplored() {
//        return this.explored.size();
//    }
//
//    public float timeSpent() {
//        return (System.currentTimeMillis() - this.startTime) / 1000f;
//    }
//
//    public String searchStatus() {
//        return String.format("#Explored: %,6d, #Frontier: %,6d, #Generated: %,6d, Time: %3.2f s \t%s", this.countExplored(), this.countFrontier(), this.countExplored()+this.countFrontier(), this.timeSpent(), Memory.stringRep());
//    }
//
//    public Plan getAndRemoveLeaf() {
//        Plan n = this.frontier.poll();
//        this.frontierSet.remove(n);
//        return n;
//    }
//
//    public void addToFrontier(Plan n) {
//        this.frontier.add(n);
//        this.frontierSet.add(n);
//    }
//
//    public boolean isExplored(Plan n) {
//        return this.explored.contains(n);
//    }
//
//    public int countFrontier() {
//        return this.frontier.size();
//    }
//
//    public void addToExplored(Plan n) {
//        this.explored.add(n);
//    }
//
//    public boolean frontierIsEmpty() {
//        return this.frontier.isEmpty();
//    }
//
//    public boolean inFrontier(Plan n) {
//        return this.frontierSet.contains(n);
//    }
//}
