package client;

import client.config.Config;
import client.config.ConfigParser;
import client.definitions.AHeuristic;
import client.definitions.AMessagePolicy;
import client.definitions.AStrategy;
import client.graph.Command;
import client.state.State;

public class Main {
    public static void main(String[] args) throws Exception {
        // read state
        ServerIO serverIO = new ServerIO("soulman");

        State initialState = serverIO.readState();
        String configPath = args.length < 1 ? "src/configs/default.config" : args[0];

        // debug
        //State initialState = ServerIO.readFromFile("src/levels/custom/MACorridors1.lvl");
        //String configPath = "src/configs/maa_stsp_pn3.config";

        Config config = ConfigParser.readConfigFromFile(configPath, initialState);

        AHeuristic heuristic = config.getHeuristic();
        AStrategy strategy = config.getStrategy();
        AMessagePolicy messagePolicy = config.getMessagePolicy();

        serverIO.sendComment("Using strategy: " + strategy.toString());
        serverIO.sendComment("Using heuristic: " + heuristic.toString());
        serverIO.sendComment("Using message policy: " + messagePolicy.toString());

        // find plan
        int h = heuristic.h(initialState);
        initialState.setH(h);
        Solution solution;
        try {
            solution = strategy.plan(initialState);
        } catch (OutOfMemoryError exc) {
            // do not change this since the performance tool expects a specific format
            System.err.println("Maximum memory usage exceeded.");
            return;
        }

        // print performance stats
        PerformanceStats stats = solution.getStats();
        // do not change this since the performance tool expects a specific format
        serverIO.sendComment(stats.getMemoryUsed());
        serverIO.sendComment(stats.getTimeSpent());
        serverIO.sendComment(stats.getSolutionLength());
        serverIO.sendComment(stats.getMessagesSent());
        serverIO.sendComment(stats.getNodesExplored());
        serverIO.sendComment(stats.getNodesGenerated());

        // send joint actions to server
        for (Command[] jointAction : solution.getPlan()) {
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
