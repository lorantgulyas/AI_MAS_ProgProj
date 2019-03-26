package client;

import client.config.Config;
import client.config.ConfigParser;

public class Main {
    public static void main(String[] args) throws Exception {
        String configPath = args.length < 1 ? "src/configs/default.config" : args[0];
        Config config = ConfigParser.readConfigFromFile(configPath);

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
