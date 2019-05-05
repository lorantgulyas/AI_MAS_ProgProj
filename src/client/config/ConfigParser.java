package client.config;

import client.definitions.AHeuristic;
import client.definitions.AMessagePolicy;
import client.definitions.AStrategy;
import client.state.State;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ConfigParser {

    public static Config readConfig(String config, State initialState) throws
            InvalidConfigException,
            UnknownHeuristicException, UnknownStrategyException, UnknownMessagePolicyException {
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
        AHeuristic heuristic = Heuristic.parseHeuristic(entries.get("heuristic"), initialState);
        AMessagePolicy messagePolicy = MessagePolicy.parseMessagePolicy(entries.get("message_policy"), initialState);
        AStrategy strategy = Strategy.parseStrategy(entries.get("strategy"), heuristic, messagePolicy);
        return new Config(strategy, heuristic, messagePolicy);
    }

    public static Config readConfigFromFile(String path, State initialState) throws
            InvalidConfigException, IOException,
            UnknownHeuristicException, UnknownStrategyException, UnknownMessagePolicyException {
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        String config = sb.toString();
        return ConfigParser.readConfig(config, initialState);
    }
}
