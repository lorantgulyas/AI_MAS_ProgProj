package client;

import client.config.ConfigParser;
import client.graph.Command;
import client.state.Agent;
import client.state.Position;
import client.state.State;
import client.state.SubState;
import client.utils.ClosedRooms;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {

        // read state
        ServerIO serverIO = new ServerIO("soulman");

        State initialState = serverIO.readState();
        String configPath = args.length < 1 ? "src/configs/default.config" : args[0];

        // debug
        //State initialState = ServerIO.readFromFile("src/levels/comp18/MACyBot.lvl");
        //String configPath = "src/configs/maa_stsp_n2_g.config";

        String config = ConfigParser.readFile(configPath);

        // split state into potentially different closed rooms
        ClosedRooms rooms = new ClosedRooms(initialState);
        ArrayList<SubState> states = rooms.getSubStates();
        ArrayList<ClosedRoomRunner> runners = new ArrayList<>();
        int nRooms = rooms.size();
        for (int i = 0; i < nRooms; i++) {
            State state = states.get(i).getState();
            int roomSize = rooms.size(i);
            ClosedRoomRunner runner = new ClosedRoomRunner(config, state, roomSize);
            runners.add(runner);
        }

        ClosedRoomRunner first = runners.get(0);
        serverIO.sendComment("strategy: " + first.getStrategy().toString());
        serverIO.sendComment("heuristic: " + first.getHeuristic().toString());
        serverIO.sendComment("message policy: " + first.getMessagePolicy().toString());
        serverIO.sendComment("merger: " + first.getMerger().toString());
        serverIO.sendComment("distance: " + first.getDistance().toString());

        // plan each room in parallel
        long startTime = System.currentTimeMillis();
        for (ClosedRoomRunner runner : runners) {
            runner.start();
        }

        // wait for each room to finish planning
        for (ClosedRoomRunner runner : runners) {
            try {
                runner.join();
            } catch (InterruptedException exc) {
                String errorMessage = "Interrupted.";
                System.err.println(errorMessage);
            } catch (OutOfMemoryError exc) {
                String errorMessage = "Maximum memory usage exceeded.";
                System.err.println(errorMessage);
            } catch (Exception exc) {
                String errorMessage = "Unknown error.";
                System.err.println(errorMessage);
            }
        }

        // merge stats
        long explored = 0;
        long generated = 0;
        long messages = 0;
        for (ClosedRoomRunner runner : runners) {
            PerformanceStats stats = runner.getSolution().getStats();
            explored += stats.nodesExplored();
            generated += stats.nodesGenerated();
            messages += stats.messagesSent();
        }

        // find longest plan
        int solutionLength = 0;
        ArrayList<Command[][]> plans = new ArrayList<>();
        for (ClosedRoomRunner runner : runners) {
            Command[][] plan = runner.getSolution().getPlan();
            plans.add(plan);
            solutionLength = Math.max(solutionLength, plan.length);
        }

        // merge plans
        Agent[] agents = initialState.getAgents();
        int nAgents = agents.length;
        Command[][] solution = new Command[solutionLength][nAgents];
        for (int i = 0; i < nRooms; i++) {
            SubState state = states.get(i);
            Command[][] plan = plans.get(i);
            int nSubAgents = state.getState().getAgents().length;
            for (int j = 0; j < plan.length; j++) {
                Command[] jointAction = plan[j];
                for (int k = 0; k < nSubAgents; k++) {
                    int agentID = state.getOriginalAgentID(k);
                    solution[j][agentID] = jointAction[k];
                }
            }
            // fill remaining cells with NoOps
            for (int j = plan.length; j < solutionLength; j++) {
                for (int k = 0; k < nSubAgents; k++) {
                    int agentID = state.getOriginalAgentID(k);
                    solution[j][agentID] = Command.NoOp;
                }
            }
        }

        // print performance stats
        PerformanceStats stats = new PerformanceStats(messages, explored, generated);
        double memoryUsed = Memory.used();
        double timeSpent = PerformanceStats.timeSpent(startTime);

        // do not change this since the performance tool expects a specific format
        serverIO.sendComment(stats.getMemoryUsed(memoryUsed));
        serverIO.sendComment(stats.getTimeSpent(timeSpent));
        serverIO.sendComment(stats.getSolutionLength(solutionLength));
        serverIO.sendComment(stats.getMessagesSent());
        serverIO.sendComment(stats.getNodesExplored());
        serverIO.sendComment(stats.getNodesGenerated());

        // send joint actions to server
        for (Command[] jointAction : solution) {
            boolean[] jointActionResponse = serverIO.sendJointAction(jointAction);
            for (boolean actionResponse : jointActionResponse) {
                if (!actionResponse) {
                    // TODO: figure out how to fix plan that went wrong...
                    throw new ServerRejectedJointActionException();
                }
            }
        }
    }
}
