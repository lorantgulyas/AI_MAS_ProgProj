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
* heuristic
    - floodfill
    - manhattan
    - single-tasker-manhattan
    - single-tasker-shortest-path
* message_policy
    - broadcast

### Example configuration file
```
strategy: cooperative_astar
heuristic: manhattan
message_policy: broadcast
```
