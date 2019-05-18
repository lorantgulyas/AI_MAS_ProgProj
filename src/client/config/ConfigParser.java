package client.config;

import client.definitions.*;
import client.state.State;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ConfigParser {

    public static Config readConfig(String config, State initialState, int stateSize) throws
            InvalidConfigException,
            UnknownHeuristicException, UnknownStrategyException, UnknownMessagePolicyException,
            UnknownMergerException, UnknownDistanceException {
        HashMap<String, String> entries = new HashMap<>();
        // strip all whitespace (except new lines)
        String stripped = config.replaceAll(" ", "");
        // find all keys ending with : and then by a value ending with newline or nothing
        String[] lines = stripped.split("\n");
        for (String line : lines) {
            String[] keyValues = line.split(":");
            if (keyValues.length != 2) {
                throw new InvalidConfigException();
            }
            String key = keyValues[0];
            String value = keyValues[1];
            entries.put(key, value);
        }
        ADistance distance = Distance.parseDistamce(entries.get("distance"), initialState);
        AHeuristic heuristic = Heuristic.parseHeuristic(entries.get("heuristic"), initialState, distance, stateSize);
        AMessagePolicy messagePolicy = MessagePolicy.parseMessagePolicy(entries.get("message_policy"), initialState, distance);
        AMerger merger = Merger.parseMerger(entries.get("merger"));
        AStrategy strategy = Strategy.parseStrategy(entries.get("strategy"), heuristic, messagePolicy, merger);
        return new Config(strategy, heuristic, messagePolicy, merger, distance);
    }

    public static Config readConfigFromFile(String path, State initialState, int stateSize) throws
            InvalidConfigException, IOException,
            UnknownHeuristicException, UnknownStrategyException, UnknownMessagePolicyException,
            UnknownMergerException, UnknownDistanceException {
        String config = ConfigParser.readFile(path);
        return ConfigParser.readConfig(config, initialState, stateSize);
    }

    public static String readFile(String path) throws IOException {
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        return sb.toString();
    }

}
