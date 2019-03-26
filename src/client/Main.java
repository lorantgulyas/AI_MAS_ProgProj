package client;

import client.config.Config;
import client.config.ConfigParser;
import client.heuristics.Manhattan;

public class Main {
    public static final IServerIO serverIO;

    public static void main(String[] args) throws Exception{
        // read config
        String configPath = args.length < 1 ? "src/configs/default.config" : args[0];
        Config config = ConfigParser.readConfigFromFile(configPath);

        // read state and setup strategy/heuristic
        State initialState = Main.serverIO.readState();
        IHeuristic heuristic = Main.getHeuristic(config);
        heuristic.preprocess(initialState);
        IStrategy strategy = Main.getStrategy(config);

        Main.serverIO.sendComment("Using strategy: " + strategy.toString());
        Main.serverIO.sendComment("Using heuristic: " + heuristic.toString());

        // find plan and send joint actions to server
        Command[][] jointActions = strategy.plan(initialState, heuristic);
        for (Command[] jointAction : jointActions) {
            boolean[] joinActionResponse = Main.serverIO.sendJointAction(jointAction);
            for (boolean actionResponse : joinActionResponse) {
                if (!actionResponse) {
                    // TODO: figure out how to fix plan that went wrong...
                    throw new ServerRejectedJointActionException();
                }
            }
        }
    }

    static IHeuristic getHeuristic(Config config) throws Exception {
        switch (config.getHeuristic()) {
            case MANHATTAN: return new Manhattan();
            default: throw new Exception();
        }
}

    static IStrategy getStrategy(Config config) throws Exception {
        switch (config.getStrategy()) {
            case COOPERATIVE_ASTAR: return new CooperativeAStar();
            default: throw new Exception();
        }
    }
}
