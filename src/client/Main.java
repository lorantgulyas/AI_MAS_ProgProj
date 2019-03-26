package client;

import client.config.Config;
import client.config.ConfigParser;
import client.definitions.AHeuristic;
import client.definitions.AServerIO;
import client.definitions.AState;
import client.definitions.AStrategy;
import client.heuristics.Manhattan;

public class Main {
    public static void main(String[] args) throws Exception{
        // read config
        String configPath = args.length < 1 ? "src/configs/default.config" : args[0];
        Config config = ConfigParser.readConfigFromFile(configPath);

        // read state and setup strategy/heuristic
        ServerIO serverIO = new ServerIO("soulman");
        AState initialState = serverIO.readState();
        AHeuristic heuristic = Main.getHeuristic(config, initialState);
        AStrategy strategy = Main.getStrategy(config, heuristic);

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

    static AHeuristic getHeuristic(Config config, AState initialState) throws Exception {
        switch (config.getHeuristic()) {
            case MANHATTAN: return new Manhattan(initialState);
            default: throw new Exception();
        }
    }

    static AStrategy getStrategy(Config config, AHeuristic heuristic) throws Exception {
        switch (config.getStrategy()) {
            case COOPERATIVE_ASTAR: return new CooperativeAStar(heuristic);
            default: throw new Exception();
        }
    }
}
