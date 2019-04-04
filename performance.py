import argparse
from datetime import datetime
import json
import os
import re
import subprocess
import sys
from tqdm import tqdm


def main():
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M")
    args = parse_arguments(timestamp)
    out_dir = '/'.join(args.out.split('/')[:-1])
    makedirs(out_dir)

    timeout = None if args.timeout is None else args.timeout[0]
    levels = os.listdir(args.levels)
    configs = os.listdir(args.configs)
    stats = []

    configuration_text = 'configuration' if len(configs) == 1 else 'configurations'
    level_text = 'level' if len(levels) == 1 else 'levels'
    print(f'Running {len(configs)} {configuration_text} on {len(levels)} {level_text}.')

    for config_name in configs:
        config_path = os.path.join(args.configs, config_name)
        for level_name in tqdm(levels, desc=config_name, unit='level'):
            level_path = os.path.join(args.levels, level_name)
            run_stats = run_client(config_path, level_path, timeout)
            run_stats['config'] = config_name
            run_stats['level'] = level_name
            stats.append(run_stats)

    output = {
        "stats": stats,
        "timestamp": timestamp,
    }
    with open(args.out, 'w') as f:
        json.dump(output, f)

    print('Done!')


def run_client(config, level, timeout):
    client = f'java -classpath out/production/programming-project client.Main {config}'
    args = ['java', '-jar', 'server.jar', '-c', client, '-l', level]
    try:
        process = subprocess.run(
            args,
            encoding='utf-8',
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            timeout=timeout,
            check=True,
            shell=False
        )
    except subprocess.CalledProcessError as exc:
        return {
            "type": "crash",
            "code": exc.returncode,
        }
    except subprocess.TimeoutExpired as exc:
        return {
            "type": "timeout",
            "timeout": timeout,
        }

    error = process.stderr
    output = process.stdout

    if 'Maximum memory usage exceeded.' in error:
        return {
            "type": "out_of_memory",
        }

    fail = {
        "type": "fail",
    }

    solved_match = re.search('\\[server\\]\\[info\\] Level solved: Yes.', output)
    solved = solved_match is not None

    nodes_explored_match = re.search('Nodes explored: (\\d+)', output)
    if nodes_explored_match is None:
        return fail
    nodes_explored = int(nodes_explored_match[1])

    nodes_generated_match = re.search('Nodes generated: (\\d+)', output)
    if nodes_generated_match is None:
        return fail
    nodes_generated = int(nodes_generated_match[1])

    solution_length_match = re.search('Solution length: (\\d+)', output)
    if solution_length_match is None:
        return fail
    solution_length = int(solution_length_match[1])

    memory_used_match = re.search('Memory used: (\\d+)(,|\\.)(\\d+) MB', output)
    if memory_used_match is None:
        return fail
    memory_used = float(f'{memory_used_match[1]}.{memory_used_match[3]}')

    time_spent_match = re.search('Time spent: (\\d+)(,|\\.)(\\d+) seconds', output)
    if time_spent_match is None:
        return fail
    time_spent = float(f'{time_spent_match[1]}.{time_spent_match[3]}')

    return {
        "type": "success",
        "solved": solved,
        "nodes_explored": nodes_explored,
        "nodes_generated": nodes_generated,
        "solution_length": solution_length,
        "memory_used": memory_used,
        "time_spent": time_spent,
    }


def makedirs(path):
    if not os.path.exists(path):
        os.makedirs(path)


def parse_arguments(timestamp):
    parser = argparse.ArgumentParser(description='Get performance stats of AI client.')
    parser.add_argument(
        '--configs',
        nargs=1,
        default='src/configs',
        help='Path to configs to run performance for (defaults to src/configs).',
        type=str,
        dest='configs',
    )
    parser.add_argument(
        '--levels',
        nargs=1,
        default='src/levels',
        help='Path to levels to run performance against (defaults to src/levels).',
        type=str,
        dest='levels',
    )
    parser.add_argument(
        '--out',
        nargs=1,
        default=f'stats/{timestamp.replace(":", "-")}.json',
        help='Path to output file (defaults to stats/{timestamp}.json)',
        type=str,
        dest='out',
    )
    parser.add_argument(
        '--timeout',
        nargs=1,
        default=None,
        help='Max. time in seconds for which a client can run. Default to infinity.',
        type=int,
        dest='timeout',
    )
    return parser.parse_args(sys.argv[1:])


if __name__ == '__main__':
    main()
