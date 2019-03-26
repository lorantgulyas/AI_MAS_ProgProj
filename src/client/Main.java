package client;

public class Main {
    public static void main(String[] args) throws Exception {
        ServerIO kappa = new ServerIO("soulman");

        kappa.sendComment("hello world");
        kappa.sendComment("kappapride");
        Command[] commands1 = { new Command(Command.Dir.W), new Command(Command.Dir.E) };
        Command[] commands2 = { new Command(Command.Dir.S), new Command(Command.Dir.E) };

        kappa.sendJointAction(commands1);
        kappa.sendJointAction(commands1);
        kappa.sendJointAction(commands2);

        // TODO:
        // read config (and override with command line args)
        // level = server input from stdin
        // initialState = parser(level)
        // solution = Search(initialState)
        // did solution work?
        //   - then send solution to server
        //   - otherwise throw a relevant error message
        System.err.println("Main is not implemented yet!");
    }
}
