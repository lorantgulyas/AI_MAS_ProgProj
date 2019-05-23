package client.config;

import client.definitions.*;
import client.path.AllObjectsAStar;
import client.path.WallOnlyAStar;
import client.state.Box;
import client.state.Goal;
import client.state.State;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class ConfigParser {

    public static Config readConfig(String config, State initialState, int stateSize, int[] agentIDMap) throws
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
        AllObjectsAStar allObjectsAStar = new AllObjectsAStar(stateSize);
        WallOnlyAStar wallOnlyAStar = new WallOnlyAStar(stateSize);
        ADistance distance = Distance.parseDistamce(entries.get("distance"), initialState, stateSize, allObjectsAStar, wallOnlyAStar);
        HashMap<Goal, Integer> goalBoxMap = ConfigParser.getGoalBoxMap(initialState, distance);
        AHeuristic heuristic = Heuristic.parseHeuristic(entries.get("heuristic"), initialState, distance, stateSize, allObjectsAStar, wallOnlyAStar, goalBoxMap);
        AMessagePolicy messagePolicy = MessagePolicy.parseMessagePolicy(entries.get("message_policy"), initialState, distance, allObjectsAStar, wallOnlyAStar, goalBoxMap);
        AMerger merger = Merger.parseMerger(entries.get("merger"));
        AStrategy strategy = Strategy.parseStrategy(entries.get("strategy"), heuristic, messagePolicy, merger, agentIDMap);
        return new Config(strategy, heuristic, messagePolicy, merger, distance);
    }

    public static Config readConfigFromFile(String path, State initialState, int stateSize, int[] agentIDMap) throws
            InvalidConfigException, IOException,
            UnknownHeuristicException, UnknownStrategyException, UnknownMessagePolicyException,
            UnknownMergerException, UnknownDistanceException {
        String config = ConfigParser.readFile(path);
        return ConfigParser.readConfig(config, initialState, stateSize, agentIDMap);
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

    private static HashMap<Goal, Integer> getGoalBoxMap(State initialState, ADistance measurer) {
        HashMap<Goal, Integer> map = new HashMap<>();
        Goal[] goals = initialState.getLevel().getGoals();
        Box[] boxes = initialState.getBoxes();
        boolean[] usedBoxes = new boolean[boxes.length];
        Arrays.fill(usedBoxes, false);
        for (Goal goal : goals) {
            Box box = ConfigParser.findClosestBoxToGoal(initialState, goal, usedBoxes, measurer);
            usedBoxes[box.getId()] = true;
            map.put(goal, box.getId());
        }
        return map;
    }

    private static Box findClosestBoxToGoal(State state, Goal goal, boolean[] usedBoxes, ADistance measurer) {
        Box[] boxes = state.getBoxes();
        Box closestBox = null;
        int minDistance = Integer.MAX_VALUE;
        for (Box box : boxes) {
            if (!usedBoxes[box.getId()] && box.getLetter() == goal.getLetter()) {
                int distance = measurer.distance(state, box.getPosition(), goal.getPosition());
                if (closestBox == null || distance < minDistance) {
                    closestBox = box;
                    minDistance = distance;
                }
            }
        }
        return closestBox;
    }

}
