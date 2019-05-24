package client;

import client.graph.Command;
import client.state.SubState;
import subgoaler.Converter;

import java.util.ArrayList;
import java.util.concurrent.*;

public class ClosedRoomRunner extends Thread {

    private static String CONFIG1_PATH = "src/configs/maa_u_sp_bc_g.config";
    private static String CONFIG2_PATH = "src/configs/maa_gs_sp_bc_g.config";
    private static String CONFIG3_PATH = "src/configs/maa_u_sp_n4_g.config";
    private static String CONFIG4_PATH = "src/configs/maa_st_sp_bc_g.config";

    private static int CONFIG1_TIMEOUT = 10;
    private static int CONFIG2_TIMEOUT = 10;
    private static int CONFIG3_TIMEOUT = 10;

    private client.state.State state;
    private SubState subState;
    private int roomSize;

    private Solution solution;

    /**
     * Class that plans in a thread.
     *
     * @param state The partitioned substate.
     * @param roomSize Number of non-wall cells in the substate.
     * @throws Exception
     */
    ClosedRoomRunner(SubState state, int roomSize) throws Exception  {
        this.subState = state;
        this.state = state.getState();
        this.roomSize = roomSize;
    }

    Solution getSolution() {
        return this.solution;
    }

    public void run() {
        int nAgents = this.state.getAgents().length;
        try {
            this.solution = nAgents == 1 ? this.singleAgent() : this.multiAgent();
        } catch (Exception exc) {
            // this should not happen
            System.err.println(exc);
            this.solution = null;
        }
    }

    private Solution multiAgent() throws Exception {
        ExecutorService executor;

        // attempt first configuration
        executor = Executors.newSingleThreadExecutor();
        ConfigRunner config1 = new ConfigRunner(ClosedRoomRunner.CONFIG1_PATH, this.subState, this.roomSize);
        Future<Solution> future1 = executor.submit(config1);
        try {
            Solution solution = future1.get(ClosedRoomRunner.CONFIG1_TIMEOUT, TimeUnit.SECONDS);
            executor.shutdown();
            return solution;
        } catch (TimeoutException exc) {
            future1.cancel(true);
        } finally {
            executor.shutdown();
        }

        // attempt second configuration
        executor = Executors.newSingleThreadExecutor();
        ConfigRunner config2 = new ConfigRunner(ClosedRoomRunner.CONFIG2_PATH, this.subState, this.roomSize);
        Future<Solution> future2 = executor.submit(config2);
        try {
            Solution solution = future2.get(ClosedRoomRunner.CONFIG2_TIMEOUT, TimeUnit.SECONDS);
            executor.shutdown();
            return solution;
        } catch (TimeoutException exc) {
            future2.cancel(true);
        } finally {
            executor.shutdown();
        }

        // attempt third configuration
        executor = Executors.newSingleThreadExecutor();
        ConfigRunner config3 = new ConfigRunner(ClosedRoomRunner.CONFIG3_PATH, this.subState, this.roomSize);
        Future<Solution> future3 = executor.submit(config3);
        try {
            Solution solution = future3.get(ClosedRoomRunner.CONFIG3_TIMEOUT, TimeUnit.SECONDS);
            executor.shutdown();
            return solution;
        } catch (TimeoutException exc) {
            future3.cancel(true);
        } finally {
            executor.shutdown();
        }

        // attempt foruth and final configuration
        ConfigRunner config4 = new ConfigRunner(ClosedRoomRunner.CONFIG4_PATH, this.subState, this.roomSize);
        return config4.call();
    }

    private Solution singleAgent() {
        subgoaler.State SAstate = Converter.convert(state);

        subgoaler.Floodfill ff = new subgoaler.Floodfill(SAstate);
        ff.findRooms(SAstate);
        ff.prioritizeGoals(SAstate);
        ff.prioritizeBoxes(SAstate);

        subgoaler.SerializedAStar sas = new subgoaler.SerializedAStar(ff);
        ArrayList<subgoaler.Command> cmds = sas.serializedPlan(SAstate);
        Command[][] clientCmds = Converter.convertSolution(cmds);

        long explored = sas.nodesExplored();
        long generated = sas.nodesGenerated();
        PerformanceStats stats = new PerformanceStats(0, explored, generated);

        return new Solution(clientCmds, stats);
    }

}
