package client;

import client.graph.Command;
import client.state.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ServerIO {
    private BufferedReader reader;
    private String name;

    public ServerIO(String clientName) {
        this.name = clientName;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public State readState() throws Exception {
        // send client name
        System.out.println(name);

        // parse file content
        return parseState(reader);
    }

    public static State readFromFile(String filename) throws Exception {
        FileReader fr = new FileReader(filename);
        BufferedReader br = new BufferedReader(fr);
        return ServerIO.parseState(br);
    }

    public static State parseState(BufferedReader reader) throws Exception {
        // read dummy lines
        for (int i = 0; i < 5; i++)
            reader.readLine();

        // fetch colors
        String line = reader.readLine();
        HashMap<Integer, Color> agentColors = new HashMap<>();
        HashMap<Character, Color> boxColors = new HashMap<>();
        while (!line.equals("#initial")) {
            String[] parts = line.split(":");
            Color color = Color.valueOf(parts[0].toUpperCase());
            String[] items = parts[1].split(",");
            for (String item : items) {
                char c = item.trim().charAt(0);
                if (c >= '0' && c <= '9') {
                    agentColors.put(Character.getNumericValue(c), color);
                } else if (c >= 'A' && c <= 'Z') {
                    boxColors.put(c, color);
                } else {
                    throw new Exception("Unknown character:" + item);
                }
            }
            line = reader.readLine();
        }

        // parse initial state
        line = reader.readLine();
        ArrayList<String> rawLevel = new ArrayList<>();
        while (!line.equals("#goal")) {
            rawLevel.add(line);
            line = reader.readLine();
        }

        // create wall matrix, agents, boxes
        Agent[] agents = new Agent[agentColors.size()];
        ArrayList<Box> boxes = new ArrayList<>();
        int boxIndex = 0;
        int rowCount = rawLevel.size();

        int maxColCount = 0;
        for (String row : rawLevel) {
            maxColCount = Math.max(maxColCount, row.length());
        }

        boolean[][] walls = new boolean[maxColCount][rowCount];
        for (int y = 0; y < rowCount; y++) {
            char[] rowChars = rawLevel.get(y).toCharArray();
            int colCount = rowChars.length;
            for (int x = 0; x < colCount; x++) {
                char c = rowChars[x];
                walls[x][y] = false;
                // block
                if (c == '+') {
                    walls[x][y] = true;
                } else if (c >= '0' && c <= '9') {
                    int agentId = Character.getNumericValue(c);
                    agents[agentId] = new Agent(agentId,
                            agentColors.get(agentId), new Position(x, y));
                } else if (c >= 'A' && c <= 'Z') {
                    boxes.add(new Box(boxIndex, c, boxColors.get(c), new Position(x, y)));
                    boxIndex++;
                } else if (c != ' ') {
                    throw new Error("Unknown character: " + c);
                }
            }
            for (int x = colCount; x < maxColCount; x++) {
                walls[x][y] = true;
            }
        }

        // some agents may have been specified but not actually used in the level
        // we fix this by removing those agents
        ArrayList<Agent> usedAgents = new ArrayList<>();
        int agentID = 0;
        for (int i = 0; i < agents.length; i++) {
            Agent agent = agents[i];
            if (agent != null) {
                Agent usedAgent = new Agent(agentID, agent.getColor(), agent.getPosition());
                usedAgents.add(usedAgent);
                agentID++;
            }
        }
        agents = usedAgents.toArray(new Agent[0]);

        // parse goal state
        line = reader.readLine();
        rawLevel = new ArrayList<>();
        while (!(line.equals("#end") || line.equals(""))) {
            rawLevel.add(line);
            line = reader.readLine();
        }

        // create wall matrix, agents, boxes
        ArrayList<Goal> goalsList = new ArrayList<>();
        ArrayList<AgentGoal> agentEndPositionsList = new ArrayList<>();
        for (int y = 0; y < rowCount; y++) {
            char[] rowChars = rawLevel.get(y).toCharArray();
            int colCount = rowChars.length;
            for (int x = 0; x < colCount; x++) {
                char c = rowChars[x];
                if (c >= 'A' && c <= 'Z') {
                    goalsList.add(new Goal(c, new Position(x, y), boxColors.get(c)));
                } else if (c >= '0' && c <= '9') {
                    int agentEndPositionID = Character.getNumericValue(c);
                    AgentGoal agentEndPosition = new AgentGoal(agentEndPositionID , new Position(x, y));
                    agentEndPositionsList.add(agentEndPosition);
                }
            }
        }

        Goal[] goals = goalsList.toArray(new Goal[0]);
        AgentGoal[] agentEndPositions = agentEndPositionsList.toArray(new AgentGoal[0]);
        Level level = new Level(walls, rowCount, maxColCount, goals, agentEndPositions);

        // get agents goals
        Goal[][] agentsGoals = new Goal[agents.length][];
        for (Agent agent : agents) {
            ArrayList<Goal> agentGoals = new ArrayList<>();
            for (Goal goal : goals) {
                if (goal.getColor() == agent.getColor()) {
                    agentGoals.add(goal);
                }
            }
            agentsGoals[agent.getId()] = agentGoals.toArray(new Goal[0]);
        }
        level.setAgentsGoals(agentsGoals);

        return new State(level, agents, boxes.toArray(new Box[0]));
    }

    public boolean[] sendJointAction(Command[] jointAction) throws Exception {
        String actions = Arrays.stream(jointAction)
                .map(Command::toString)
                .collect(Collectors.joining(";"));
        System.out.println(actions);
        boolean[] res = new boolean[jointAction.length];
        String[] split =  reader.readLine().split(";");
        for (int i = 0; i < jointAction.length; i++)
            res[i] = Boolean.parseBoolean(split[i]);
        return res;
    }

    public void sendComment(String comment) {
        System.out.println("#" + comment);
    }
}
