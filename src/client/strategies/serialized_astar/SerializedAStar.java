//package client.strategies.serialized_astar;
//
//import client.graph.Action;
//import client.graph.Command;
//import client.graph.Plan;
//import client.graph.PlanComparator;
//import client.state.Goal;
//import client.state.Level;
//import client.state.Position;
//import client.state.State;
//
//import java.util.*;
//
//public class SerializedAStar {
//
//    private Floodfill ff;
//    private Plan subresult;
//
//    private int explored;
//    private int generated;
//
//    public SerializedAStar(Floodfill ff) {
//        this.ff = ff;
//        this.explored = 0;
//        this.generated = 0;
//    }
//
//    public int nodesGenerated() {
//        return this.generated;
//    }
//
//    public int nodesExplored() {
//        return this.explored;
//    }
//
//    public ArrayList<Command> serializedPlan(State init) {
//        Level level = init.getLevel();
//        Goal[] allGoals = level.getGoals();
//        ArrayList<Command> cmds = new ArrayList<>();
//        subresult = new Plan(init);
//        for (int i = 0; i < allGoals.length; i++) {
////        for (int i = 0; i < 1; i++) {
////            if (i == 59) {
////                System.err.println("kek");
////            }
//            level.setGoals(Arrays.copyOfRange(allGoals, 0, i + 1));
//            System.err.println("--- task: " + i + ", goal: " + allGoals[i]);
//
//            ArrayList<Command> subCmds;
//
//            try {
//                subCmds = plan(subresult);
//            } catch (BlockedException e) {
//                System.err.println("freeing path for " + allGoals[i]);
//                ArrayList<Position> path = ff.findPath(allGoals[i].getPosition(),
//                        subresult.getState().getBoxes()[i].getPosition());
//                // path does not contain goal itself we have to add it manually
//                path.add(allGoals[i].getPosition());
//                subCmds = freePath(subresult, path);
//                try {
//                    subCmds.addAll(plan(subresult));
//                } catch (BlockedException ex) {
//                    System.err.println("trying again: " + allGoals[i]);
//                    System.err.println("we failed big times");
//                }
//            }
//
//            cmds.addAll(subCmds);
//
//            //            for (Command cmd : subCmds) {
////                System.err.println(cmd);
////            }
//        }
//        return cmds;
//    }
//
//    public ArrayList<Command> freePath(Plan root, ArrayList<Position> path) {
//        Comparator<Plan> comp = (n1, n2) ->
//                n1.g() + ff.hPath(n1.getState(), path) - n2.g() - ff.hPath(n2.getState(), path);
//        AStar strategy = new AStar(comp);
//        strategy.addToFrontier(root);
//
//        int iterations = 0;
//        while (true) {
//            if (iterations % 10000 == 0) {
//                System.err.println("i: " + iterations + ", " + strategy.searchStatus());
//            }
//
//            if (strategy.frontierIsEmpty()) {
//                return null;
//            }
//
//            Plan leafState = strategy.getAndRemoveLeaf();
//
//            // isGoalState
//            if (ff.hPath(leafState.getState(), path) == 0) {
//                Action[] res = leafState.extract();
//                subresult = leafState.makeRoot();
//
//                ArrayList<Command> commands = new ArrayList<>(res.length);
//                for (Action action : res) {
//                    commands.add(action.getCommand());
//                }
//                return commands;
//            }
//
//            strategy.addToExplored(leafState);
//            this.explored++;
//            for (Plan n : leafState.getChildren(this.ff, 0)) { // The list of expanded states is shuffled randomly; see State.java.
//                if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
//                    strategy.addToFrontier(n);
//                    this.generated++;
//                }
//            }
//            iterations++;
//        }
//    }
//
//    public ArrayList<Command> plan(Plan init) throws BlockedException {
//        PlanComparator comparator = new PlanComparator();
//        AStar strategy = new AStar(comparator::compare);
//        strategy.addToFrontier(init);
//
//        int iterations = 0;
//        while (true) {
//            if (iterations % 10000 == 0) {
//                System.err.println("i: " + iterations + ", " + strategy.searchStatus());
//                if (iterations == 40000) {
//                    throw new BlockedException();
//                }
//            }
//
//            if (strategy.frontierIsEmpty()) {
//                return null;
//            }
//
//            Plan leafState = strategy.getAndRemoveLeaf();
//
//            // debug stuff starts
////            int g = leafState.g();
////            int h = ff.h(leafState);
////            Position agentPos = leafState.getAgents()[0].getPosition();
////            Position boxPos = leafState.getBoxes()[State.getGoals().length - 1].getPosition();
//            // debug stuff ends
//
//            if (leafState.getState().isGoalState()) {
//                Action[] res = leafState.extract();
//                subresult = leafState.makeRoot();
//
//                ArrayList<Command> commands = new ArrayList<>(res.length);
//                for (Action action : res) {
//                    commands.add(action.getCommand());
//                }
//                return commands;
//            }
//
//            strategy.addToExplored(leafState);
//            this.explored++;
//            for (Plan n : leafState.getChildren(this.ff, 0)) { // The list of expanded states is shuffled randomly; see State.java.
//                if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
//                    strategy.addToFrontier(n);
//                    this.generated++;
//                }
//            }
//            iterations++;
//        }
//    }
//
//}
//
