package client;

import client.graph.Action;
import client.graph.ActionComparator;
import client.graph.ActionGenerator;
import client.graph.Command;
import client.state.Position;
import client.state.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;

public class ActionGeneratorTest {

    private State MAAgentBlockedPath;
    private State MAAgentBlockedPathConstrained;
    private State MABoxBlockedPath;
    private State MABoxBlockedPathConstrained;
    private State MABoxBlockedPath2;
    private State MABoxBlockedPath2Constrained;
    private State SA3x3Box;
    private State SA3x3PullNorth;
    private State SA3x3PullEast;
    private State SA3x3PullSouth;
    private State SA3x3PullWest;
    private State SA5x5Push;
    private State SASingleCellBox;

    public ActionGeneratorTest() {
        try {
            this.MAAgentBlockedPath = ServerIO.readFromFile("test/levels/MAAgentBlockedPath.lvl");
            this.MAAgentBlockedPathConstrained = ServerIO.readFromFile("test/levels/MAAgentBlockedPathConstrained.lvl");
            this.MABoxBlockedPath = ServerIO.readFromFile("test/levels/MABoxBlockedPath.lvl");
            this.MABoxBlockedPathConstrained = ServerIO.readFromFile("test/levels/MABoxBlockedPathConstrained.lvl");
            this.MABoxBlockedPath2 = ServerIO.readFromFile("test/levels/MABoxBlockedPath2.lvl");
            this.MABoxBlockedPath2Constrained = ServerIO.readFromFile("test/levels/MABoxBlockedPath2Constrained.lvl");
            this.SA3x3Box = ServerIO.readFromFile("test/levels/SA3x3Box.lvl");
            this.SA3x3PullNorth = ServerIO.readFromFile("test/levels/SA3x3PullNorth.lvl");
            this.SA3x3PullEast = ServerIO.readFromFile("test/levels/SA3x3PullEast.lvl");
            this.SA3x3PullSouth = ServerIO.readFromFile("test/levels/SA3x3PullSouth.lvl");
            this.SA3x3PullWest = ServerIO.readFromFile("test/levels/SA3x3PullWest.lvl");
            this.SA5x5Push = ServerIO.readFromFile("test/levels/SA5x5Push.lvl");
            this.SASingleCellBox = ServerIO.readFromFile("test/levels/SASingleCellBox.lvl");
        } catch (Exception exc) {
            System.out.println(exc);
        }
    }

    /*
     * Tests:
     * 1.  children generates only NoOp in a single cell box
     *       - children
     *       - constrainedChildren
     * 2.  generates all move actions and a NoOp in a 3x3 box
     *       - children
     *       - constrainedChildren
     * 3.  does not generate move conflicts
     *       - children
     *       - constrainedChildren
     * 4.  does not generate push conflicts
     *       - children
     *       - constrainedChildren
     * 5.  does not generate pull conflicts
     *       - children
     *       - constrainedChildren
     * 6.  generates all push commands when surrounded by boxes
     *       - children
     *       - constrainedChildren
     * 7.  generates all pull commands to north
     *       - children
     *       - constrainedChildren
     * 8.  generates all pull commands to east
     *       - children
     *       - constrainedChildren
     * 9.  generates all pull commands to south
     *       - children
     *       - constrainedChildren
     * 10. generates all pull commands to west
     *       - children
     *       - constrainedChildren
     * 11. constrainedChildren will not push to constraints
     * 12. constrainedChildren will not push constrained box
     * 13. constrainedChildren will not pull to constraints
     * 14. constrainedChildren will not pull constrained box
     */

    @Test
    public void childrenGeneratesOnlyNoOpInAClosedBox() {
        Command[] expected = new Command[] {  Command.NoOp };
        ArrayList<Action> actual = ActionGenerator.children(this.SASingleCellBox, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void constrainedChildrenGeneratesOnlyNoOpInAClosedBox() {
        Command[] expected = new Command[] {  Command.NoOp };
        HashSet<Position> constraints = new HashSet<>();
        ArrayList<Action> actual = ActionGenerator.constrainedChildren(this.SASingleCellBox, constraints, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void childrenGeneratesAllMoveActionsAndNoOpInA3x3Box() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.N),
                new Command(Command.Dir.E),
                new Command(Command.Dir.S),
                new Command(Command.Dir.W),
        };
        ArrayList<Action> actual = ActionGenerator.children(this.SA3x3Box, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void constrainedChildrenGeneratesAllMoveActionsAndNoOpInA3x3Box() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.N),
                new Command(Command.Dir.E),
                new Command(Command.Dir.S),
                new Command(Command.Dir.W),
        };
        HashSet<Position> constraints = new HashSet<>();
        ArrayList<Action> actual = ActionGenerator.constrainedChildren(this.SA3x3Box, constraints, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void childrenDoesNotGenerateMoveConflicts() {
        Command[] expected0 = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.W)
        };
        Command[] expected1 = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.E)
        };
        ArrayList<Action> actual0 = ActionGenerator.children(this.MAAgentBlockedPath, 0);
        ArrayList<Action> actual1 = ActionGenerator.children(this.MAAgentBlockedPath, 1);
        this.assertActionsEqual(actual0, 0, expected0);
        this.assertActionsEqual(actual1, 1, expected1);
    }

    @Test
    public void constrainedChildrenDoesNotGenerateMoveConflicts() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.E)
        };
        HashSet<Position> constraints = new HashSet<>();
        constraints.add(new Position(6, 3));
        ArrayList<Action> actual = ActionGenerator.constrainedChildren(this.MAAgentBlockedPathConstrained, constraints, 1);
        this.assertActionsEqual(actual, 1, expected);
    }

    @Test
    public void childrenDoesNotGeneratePushConflicts() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.W),
                new Command(Command.Type.Pull, Command.Dir.W, Command.Dir.E)
        };
        ArrayList<Action> actual = ActionGenerator.children(this.MABoxBlockedPath, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void constrainedChildrenDoesNotGeneratePushConflicts() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.E),
        };
        HashSet<Position> constraints = new HashSet<>();
        constraints.add(new Position(5, 3));
        constraints.add(new Position(6, 3));
        ArrayList<Action> actual = ActionGenerator.constrainedChildren(this.MABoxBlockedPathConstrained, constraints, 1);
        this.assertActionsEqual(actual, 1, expected);
    }

    @Test
    public void childrenDoesNotGeneratePullConflicts() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Type.Push, Command.Dir.E, Command.Dir.E)
        };
        ArrayList<Action> actual = ActionGenerator.children(this.MABoxBlockedPath2, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void constrainedChildrenDoesNotGeneratePullConflicts() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.W)
        };
        HashSet<Position> constraints = new HashSet<>();
        constraints.add(new Position(5, 3));
        constraints.add(new Position(6, 3));
        ArrayList<Action> actual = ActionGenerator.constrainedChildren(this.MABoxBlockedPath2Constrained, constraints, 1);
        this.assertActionsEqual(actual, 1, expected);
    }

    @Test
    public void childrenGeneratesAllPushCommandsWhenSurroundedByBoxes() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Type.Push, Command.Dir.N, Command.Dir.W),
                new Command(Command.Type.Push, Command.Dir.N, Command.Dir.N),
                new Command(Command.Type.Push, Command.Dir.N, Command.Dir.E),
                new Command(Command.Type.Push, Command.Dir.E, Command.Dir.N),
                new Command(Command.Type.Push, Command.Dir.E, Command.Dir.E),
                new Command(Command.Type.Push, Command.Dir.E, Command.Dir.S),
                new Command(Command.Type.Push, Command.Dir.S, Command.Dir.E),
                new Command(Command.Type.Push, Command.Dir.S, Command.Dir.S),
                new Command(Command.Type.Push, Command.Dir.S, Command.Dir.W),
                new Command(Command.Type.Push, Command.Dir.W, Command.Dir.S),
                new Command(Command.Type.Push, Command.Dir.W, Command.Dir.W),
                new Command(Command.Type.Push, Command.Dir.W, Command.Dir.N),
        };
        ArrayList<Action> actual = ActionGenerator.children(this.SA5x5Push, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void constrainedChildrenGeneratesAllPushCommandsWhenSurroundedByBoxes() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Type.Push, Command.Dir.N, Command.Dir.W),
                new Command(Command.Type.Push, Command.Dir.N, Command.Dir.N),
                new Command(Command.Type.Push, Command.Dir.N, Command.Dir.E),
                new Command(Command.Type.Push, Command.Dir.E, Command.Dir.N),
                new Command(Command.Type.Push, Command.Dir.E, Command.Dir.E),
                new Command(Command.Type.Push, Command.Dir.E, Command.Dir.S),
                new Command(Command.Type.Push, Command.Dir.S, Command.Dir.E),
                new Command(Command.Type.Push, Command.Dir.S, Command.Dir.S),
                new Command(Command.Type.Push, Command.Dir.S, Command.Dir.W),
                new Command(Command.Type.Push, Command.Dir.W, Command.Dir.S),
                new Command(Command.Type.Push, Command.Dir.W, Command.Dir.W),
                new Command(Command.Type.Push, Command.Dir.W, Command.Dir.N),
        };
        HashSet<Position> constraints = new HashSet<>();
        ArrayList<Action> actual = ActionGenerator.constrainedChildren(this.SA5x5Push, constraints, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void childrenGeneratesAllPullCommandsToNorth() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.W),
                new Command(Command.Dir.N),
                new Command(Command.Dir.E),
                new Command(Command.Type.Pull, Command.Dir.W, Command.Dir.S),
                new Command(Command.Type.Pull, Command.Dir.N, Command.Dir.S),
                new Command(Command.Type.Pull, Command.Dir.E, Command.Dir.S),
        };
        ArrayList<Action> actual = ActionGenerator.children(this.SA3x3PullNorth, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void constrainedChildrenGeneratesAllPullCommandsToNorth() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.W),
                new Command(Command.Dir.N),
                new Command(Command.Dir.E),
                new Command(Command.Type.Pull, Command.Dir.W, Command.Dir.S),
                new Command(Command.Type.Pull, Command.Dir.N, Command.Dir.S),
                new Command(Command.Type.Pull, Command.Dir.E, Command.Dir.S),
        };
        HashSet<Position> constraints = new HashSet<>();
        ArrayList<Action> actual = ActionGenerator.constrainedChildren(this.SA3x3PullNorth, constraints, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void childrenGeneratesAllPullCommandsToEast() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.N),
                new Command(Command.Dir.E),
                new Command(Command.Dir.S),
                new Command(Command.Type.Pull, Command.Dir.N, Command.Dir.W),
                new Command(Command.Type.Pull, Command.Dir.E, Command.Dir.W),
                new Command(Command.Type.Pull, Command.Dir.S, Command.Dir.W),
        };
        ArrayList<Action> actual = ActionGenerator.children(this.SA3x3PullEast, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void constrainedChildrenGeneratesAllPullCommandsToEast() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.N),
                new Command(Command.Dir.E),
                new Command(Command.Dir.S),
                new Command(Command.Type.Pull, Command.Dir.N, Command.Dir.W),
                new Command(Command.Type.Pull, Command.Dir.E, Command.Dir.W),
                new Command(Command.Type.Pull, Command.Dir.S, Command.Dir.W),
        };
        HashSet<Position> constraints = new HashSet<>();
        ArrayList<Action> actual = ActionGenerator.constrainedChildren(this.SA3x3PullEast, constraints, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void childrenGeneratesAllPullCommandsToSouth() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.W),
                new Command(Command.Dir.E),
                new Command(Command.Dir.S),
                new Command(Command.Type.Pull, Command.Dir.W, Command.Dir.N),
                new Command(Command.Type.Pull, Command.Dir.E, Command.Dir.N),
                new Command(Command.Type.Pull, Command.Dir.S, Command.Dir.N),
        };
        ArrayList<Action> actual = ActionGenerator.children(this.SA3x3PullSouth, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void constrainedChildrenGeneratesAllPullCommandsToSouth() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.W),
                new Command(Command.Dir.E),
                new Command(Command.Dir.S),
                new Command(Command.Type.Pull, Command.Dir.W, Command.Dir.N),
                new Command(Command.Type.Pull, Command.Dir.E, Command.Dir.N),
                new Command(Command.Type.Pull, Command.Dir.S, Command.Dir.N),
        };
        HashSet<Position> constraints = new HashSet<>();
        ArrayList<Action> actual = ActionGenerator.constrainedChildren(this.SA3x3PullSouth, constraints, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void childrenGeneratesAllPullCommandsToWest() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.W),
                new Command(Command.Dir.N),
                new Command(Command.Dir.S),
                new Command(Command.Type.Pull, Command.Dir.W, Command.Dir.E),
                new Command(Command.Type.Pull, Command.Dir.N, Command.Dir.E),
                new Command(Command.Type.Pull, Command.Dir.S, Command.Dir.E),
        };
        ArrayList<Action> actual = ActionGenerator.children(this.SA3x3PullWest, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void constrainedChildrenGeneratesAllPullCommandsToWest() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.W),
                new Command(Command.Dir.N),
                new Command(Command.Dir.S),
                new Command(Command.Type.Pull, Command.Dir.W, Command.Dir.E),
                new Command(Command.Type.Pull, Command.Dir.N, Command.Dir.E),
                new Command(Command.Type.Pull, Command.Dir.S, Command.Dir.E),
        };
        HashSet<Position> constraints = new HashSet<>();
        ArrayList<Action> actual = ActionGenerator.constrainedChildren(this.SA3x3PullWest, constraints, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void constrainedChildrenWillNotPushToConstraints() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.W),
                new Command(Command.Type.Pull, Command.Dir.W, Command.Dir.E),
        };
        HashSet<Position> constraints = new HashSet<>();
        constraints.add(new Position(6, 3));
        ArrayList<Action> actual = ActionGenerator.constrainedChildren(this.MABoxBlockedPathConstrained, constraints, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void constrainedChildrenWillNotPullToConstraints() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Type.Push, Command.Dir.E, Command.Dir.E),
        };
        HashSet<Position> constraints = new HashSet<>();
        constraints.add(new Position(5, 3));
        ArrayList<Action> actual = ActionGenerator.constrainedChildren(this.MABoxBlockedPath2Constrained, constraints, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    @Test
    public void constrainedChildrenWillNotPushOrPullConstrainedBox() {
        Command[] expected = new Command[] {
                Command.NoOp,
                new Command(Command.Dir.W),
        };
        HashSet<Position> constraints = new HashSet<>();
        constraints.add(new Position(5, 3));
        ArrayList<Action> actual = ActionGenerator.constrainedChildren(this.MABoxBlockedPathConstrained, constraints, 0);
        this.assertActionsEqual(actual, 0, expected);
    }

    private void assertActionsEqual(ArrayList<Action> actual, int agentID, Command[] expectedCommands) {
        ArrayList<Action> expected = new ArrayList<>();
        for (Command command : expectedCommands) {
            expected.add(new Action(agentID, command, new HashSet<>()));
        }
        ActionComparator comparator = new ActionComparator();
        actual.sort(comparator);
        expected.sort(comparator);
        Assertions.assertIterableEquals(expected, actual);
    }

}
