package subgoaler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Client {
    private BufferedReader reader;
    private String name;

    public Client(String clientName) {
        this.name = clientName;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public State init() throws Exception {
        // send client name
        System.out.println(name);

        // parse file content
        return parseState(reader);
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
        int colCount = rawLevel.get(0).length();
        int rowCount = rawLevel.size();
        boolean[][] walls = new boolean[rowCount][colCount];
        for (int y = 0; y < rowCount; y++) {
            char[] rowChars = rawLevel.get(y).toCharArray();
            for (int x = 0; x < colCount; x++) {
                char c = rowChars[x];
                walls[y][x] = false;
                // block
                if (c == '+') {
                    walls[y][x] = true;
                } else if (c >= '0' && c <= '9') {
                    int agentId = Character.getNumericValue(c);
                    agents[agentId] = new Agent(agentId,
                            agentColors.get(agentId), new Position(x, y), null);
                } else if (c >= 'A' && c <= 'Z') {
                    boxes.add(new Box(c, boxColors.get(c), new Position(x, y)));
                } else if (c != ' ') {
                    throw new Error("Unknown character: " + c);
                }
            }
        }

        // parse goal state
        line = reader.readLine();
        rawLevel = new ArrayList<>();
        while (!(line.equals("#end") || line.equals(""))) {
            rawLevel.add(line);
            line = reader.readLine();
        }

        // create wall matrix, agents, boxes
        ArrayList<Goal> goals = new ArrayList<>();
        for (int y = 0; y < rowCount; y++) {
            char[] rowChars = rawLevel.get(y).toCharArray();
            for (int x = 0; x < colCount; x++) {
                char c = rowChars[x];
                if (c >= 'A' && c <= 'Z') {
                    goals.add(new Goal(c, new Position(x, y)));
                }
                else if (c >= '0' && c <= '9') {
                    int agentId = Character.getNumericValue(c);
                    agents[agentId].setGoalPosition(new Position(x, y));
                }
            }
        }

        return new State(agents, boxes.toArray(Box[]::new), goals.toArray(Goal[]::new), walls);
    }

    public boolean[] sendCommands(Command[] cmds) throws Exception {
        String actions = Arrays.stream(cmds)
                .map(Command::toString)
                .collect(Collectors.joining(";"));
        System.out.println(actions);
        boolean[] res = new boolean[cmds.length];
        String[] split =  reader.readLine().split(";");
        for (int i = 0; i < cmds.length; i++)
            res[i] = Boolean.parseBoolean(split[i]);
        return res;
    }

    public void sendComment(String comment) {
        System.out.println("#" + comment);
    }
}
