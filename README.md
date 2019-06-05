# AI_MAS_ProgProj
Artificitial Intelligence and Multi Agent System Programming Project, 2019 DTU

## Configurations
The client has a single optional command line argument which specifies the path to a config file. It defaults to `configs/default.config` if arguments are empty.

### Format of a configuration file
Each line specifies a single pair of a key and a value separated by a colon. Note that all whitespace are ignored and so you are not allowed to use whitespace in keys and values.

The following keys are accepted together with their accepted values:

* strategy
    - cooperative_astar
    - multi-agent_astar
    - multi-body_astar
* heuristic
    - floodfill
    - goal-seeker
    - manhattan
    - single-tasker
    - unblocker
* message_policy
    - block-change
    - broadcast
    - nearby(x)
    - public
    - public-nearby(x)
* merger
    - cells-used
    - greedy
    - no-merge
* distance
    - manhattan
    - shortest-path
    - shortest-unblocked-path

Note that x in nearby and public-nearby must be a positive integer.

### Example configuration file
```
strategy: multi-agent_astar
heuristic: goal-seeker
message_policy: broadcast
merger: greedy
distance: shortest-unblocked-path
```


### How to compile and run
There is a compiled version of our code in a JAR format `programming-project.jar`, which can be found in the root directory.

To run our compiled code with any level:
```
java -jar server.jar -c "java -jar programming-project.jar" -l "src/levels/comp19/{name_of_level}.lvl" -g
```

To run our client on every level and do benchmarking:
```
java -jar server.jar -c "java -jar programming-project.jar" -l "src/levels/comp19/" -t 180 -o "soulman.zip"
```
