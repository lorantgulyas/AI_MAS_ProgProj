package subgoaler;

import java.util.ArrayList;
import java.util.Arrays;

public class Converter {


    public static State convert(client.state.State clientState) {
        boolean[][] clientWalls = clientState.getLevel().getWalls();
        boolean[][] walls = new boolean[clientWalls[0].length][clientWalls.length];
        for (int x=0; x < clientWalls.length; x++) {
            for (int y=0; y < clientWalls[0].length; y++) {
                walls[y][x] = clientWalls[x][y];
            }
        }

        Agent[] agents = Arrays.stream(clientState.getAgents())
                .map(agent -> {
                    Position pos = new Position(agent.getPosition().getCol(),
                            agent.getPosition().getRow());
                    Position goalPos = null;
                    if (clientState.getLevel().getAgentEndPositions().length != 0) {
                        goalPos = new Position(clientState.getLevel().getAgentEndPositions()[0].getPosition().getCol(),
                                clientState.getLevel().getAgentEndPositions()[0].getPosition().getRow());
                    }
                    Color color = Color.valueOf(agent.getColor().name());
                    return new Agent(agent.getId(), color, pos, goalPos);
                })
                .toArray(Agent[]::new);

        Color agentColor = agents[0].getColor();

        Box[] boxes = Arrays.stream(clientState.getBoxes())
                .map(box -> {
                    Position pos = new Position(box.getPosition().getCol(),
                            box.getPosition().getRow());
                    Color color = Color.valueOf(box.getColor().name());
                    return new Box(box.getLetter(), color, pos);
                })
                .filter(box -> {
                    if (box.getColor() == agentColor) {
                        return true;
                    }
                    else {
                        walls[box.getPosition().getY()][box.getPosition().getX()] = true;
                        return false;
                    }

                })
                .toArray(Box[]::new);

        Goal[] goals = Arrays.stream(clientState.getLevel().getGoals())
                .map(goal -> {
                    Position pos = new Position(goal.getPosition().getCol(),
                            goal.getPosition().getRow());
                    return new Goal(goal.getLetter(), pos);
                })
                .filter(goal -> {
                    int goalX = goal.getPosition().getX();
                    int goalY = goal.getPosition().getY();
                    return !walls[goalY][goalX];
                })
                .toArray(Goal[]::new);


        //System.err.println(new State(agents, boxes, goals, walls));
        return new State(agents, boxes, goals, walls);
    }


    public static client.graph.Command[][] convertSolution(ArrayList<Command> solution) {

        return solution.stream()
                .map(command -> {
                    client.graph.Command.Type clientType =
                            client.graph.Command.Type.valueOf(command.actionType.name());
                    client.graph.Command.Dir clientDir1 =
                            client.graph.Command.Dir.valueOf(command.dir1.name());
                    client.graph.Command.Dir clientDir2 = null;
                    if (command.dir2 != null)
                            clientDir2 = client.graph.Command.Dir.valueOf(command.dir2.name());
                    client.graph.Command clientCommand =
                            new client.graph.Command(clientType, clientDir1, clientDir2);
                    client.graph.Command[] clientCommands = new client.graph.Command[1];
                    clientCommands[0] = clientCommand;
                    return clientCommands;
                })
                .toArray(client.graph.Command[][]::new);
    }
}
