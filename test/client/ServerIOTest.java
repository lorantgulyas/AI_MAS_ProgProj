package client;

import client.graph.Command;
import client.state.*;
import org.junit.jupiter.api.*;

import java.io.*;

public class ServerIOTest {

    public static final String LEVEL_PATH = "test/levels/MA3x3Box.lvl";

    private static String LEVEL_CONTENT;

    private ByteArrayInputStream inStream;
    private ByteArrayOutputStream outStream;
    private PrintStream out;

    @BeforeAll
    public static void setUp() throws Exception {
        FileReader fr = new FileReader(ServerIOTest.LEVEL_PATH);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }

        // add a joint action response
        sb.append("true;false\n");

        ServerIOTest.LEVEL_CONTENT = sb.toString();
    }

    @BeforeEach
    public void setUpEach() {
        this.inStream = new ByteArrayInputStream(ServerIOTest.LEVEL_CONTENT.getBytes());
        System.setIn(this.inStream);

        this.outStream = new ByteArrayOutputStream();
        this.out = new PrintStream(this.outStream);
        System.setOut(this.out);
    }

    @AfterEach
    public void tearDownEach() throws Exception {
        this.inStream.close();
        this.inStream = null;
        this.outStream.close();
        this.out.close();
        this.outStream = null;
        this.out = null;
    }

    @AfterAll
    public static void tearDown() {
        ServerIOTest.LEVEL_CONTENT = null;
    }

    @Test
    public void readStateDoesNotThrowAnException() {
        Assertions.assertDoesNotThrow(() -> new ServerIO("test").readState());
    }

    @Test
    public void clientSendsItsNameTerminatedByANewLine() throws Exception {
        String name = "test name";
        ServerIO io = new ServerIO(name);
        io.readState();
        String actual = this.outStream.toString();
        boolean startsWith = actual.startsWith(name + "\n");
        Assertions.assertTrue(startsWith);
    }

    @Test
    public void clientParsesLevelCorrectly() throws Exception {
        boolean[][] walls = new boolean[5][5];
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                walls[row][col] = row == 0 || row == 4 || col == 0 || col == 4;
            }
        }
        Goal[] goals = new Goal[] {
            new Goal('A', new Position(1, 3)),
            new Goal('B', new Position(3, 3)),
        };
        Level level = new Level(walls,5,  5, goals);
        Agent[] agents = new Agent[] {
            new Agent(0, Color.RED, new Position(1, 1)),
            new Agent(1, Color.GREEN, new Position(3, 1)),
        };
        Box[] boxes = new Box[] {
            new Box(0, 'A', Color.RED, new Position(1, 2)),
            new Box(1, 'B', Color.GREEN, new Position(3, 2)),
        };
        State expected = new State(level, agents, boxes);
        ServerIO io = new ServerIO("test");
        State actual = io.readState();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void clientSendsCommentsCorrectly() {
        String comment = "this is a test";
        ServerIO io = new ServerIO("test");
        io.sendComment(comment);
        String actual = this.outStream.toString();
        boolean endsWith = actual.endsWith("#" + comment + "\n");
        Assertions.assertTrue(endsWith);
    }

    @Test
    public void clientSendsAJointActionCorrectly() throws Exception {
        ServerIO io = new ServerIO("test");
        io.readState();
        io.sendJointAction(new Command[] { Command.NoOp, Command.NoOp });
        String actual = this.outStream.toString();
        boolean endsWith = actual.endsWith("NoOp;NoOp\n");
        Assertions.assertTrue(endsWith);
    }

    @Test
    public void clientParsesJointActionResponseCorrectly() throws Exception {
        ServerIO io = new ServerIO("test");
        io.readState();
        boolean[] expected = new boolean[] { true, false };
        boolean[] actual = io.sendJointAction(new Command[] {Command.NoOp, Command.NoOp });
        Assertions.assertArrayEquals(expected, actual);
    }

}