package client;

import client.config.Config;
import client.config.ConfigParser;
import client.definitions.AHeuristic;
import client.definitions.AStrategy;
import client.graph.Command;
import client.state.State;

public class Main {
    public static void main(String[] args) throws Exception {
        // read state
        ServerIO serverIO = new ServerIO("soulman");
        State initialState = serverIO.readState();

        // read config
        String configPath = args.length < 1 ? "src/configs/default.config" : args[0];
        Config config = ConfigParser.readConfigFromFile(configPath, initialState);

        AHeuristic heuristic = config.getHeuristic();
        AStrategy strategy = config.getStrategy();

        serverIO.sendComment("Using strategy: " + strategy.toString());
        serverIO.sendComment("Using heuristic: " + heuristic.toString());

        // find plan
        Command[][] jointActions;
        try {
            jointActions = strategy.plan(initialState);
        } catch (OutOfMemoryError exc) {
            System.err.println("Maximum memory usage exceeded.");
            return;
        }

        // send joint actions to server
        for (Command[] jointAction : jointActions) {
            boolean[] joinActionResponse = serverIO.sendJointAction(jointAction);
            for (boolean actionResponse : joinActionResponse) {
                if (!actionResponse) {
                    // TODO: figure out how to fix plan that went wrong...
                    throw new ServerRejectedJointActionException();
                }
            }
        }
    }
}
