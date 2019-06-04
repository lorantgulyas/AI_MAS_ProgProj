import argparse
from datetime import datetime
import json
import os
import re
import signal
import shlex
import subprocess
import sys
from tqdm import tqdm


def main():
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M")
    args = parse_arguments(timestamp)
    out_dir = '/'.join(args.out.split('/')[:-1])
    makedirs(out_dir)

    levels = os.listdir(args.levels)
    if args.configs != 'NO_CONFIG':
        configs = os.listdir(args.configs)
    else:
        configs = ['NO_CONFIG']
    stats = []

    configuration_text = 'configuration' if len(configs) == 1 else 'configurations'
    level_text = 'level' if len(levels) == 1 else 'levels'
    print(f'Running {len(configs)} {configuration_text} on {len(levels)} {level_text}.')

    for config_name in configs:
        if config_name is 'NO_CONFIG':
            config_path = None
        else:
            config_path = os.path.join(args.configs, config_name)
        for level_name in tqdm(levels, desc=config_name, unit='level'):
            level_path = os.path.join(args.levels, level_name)
            run_stats = run_client(config_path, level_path, args.timeout)
            run_stats['config'] = config_name
            run_stats['level'] = level_name
            stats.append(run_stats)

        # write to output after each configuration
        output = {
            "stats": stats,
            "timestamp": timestamp,
        }
        with open(args.out, 'w') as f:
            json.dump(output, f)

    print('Done!')


def run_client(config, level, timeout):
    base_client = 'java -classpath out -Xmx32g client.Main'
    if config is None:
        client = base_client
    else:
        client = f'{base_client} {config}'
    args = f'java -jar server.jar -c "{client}" -l "{level}"'
    args = shlex.split(args)

    try:
        process = run_process(args, timeout)
    except subprocess.CalledProcessError as exc:
        return {
            "type": "crash",
            "code": exc.returncode,
        }
    except subprocess.TimeoutExpired as exc:
        return {
            "type": "timeout",
            "timeout": exc.timeout,
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

    messages_sent_match = re.search('Messages sent: (\\d+)', output)
    if messages_sent_match is None:
        return fail
    messages_sent = int(messages_sent_match[1])

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
        "messages_sent": messages_sent,
        "memory_used": memory_used,
        "time_spent": time_spent,
    }


def run_process(args, timeout):
    """
    Modified version of subprocess.run that will terminate
    not only the process but also all the child processes
    spawned by that process
    """
    with subprocess.Popen(args, encoding='utf-8', stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                          shell=False, close_fds=True, preexec_fn=os.setsid) as process:
        try:
            stdout, stderr = process.communicate(None, timeout=timeout)
        except subprocess.TimeoutExpired:
            os.killpg(process.pid, signal.SIGUSR1)
            stdout, stderr = process.communicate()
            raise subprocess.TimeoutExpired(process.args, timeout, output=stdout, stderr=stderr)
        except:
            os.killpg(process.pid, signal.SIGUSR1)
            process.wait()
            raise
        retcode = process.poll()
        if retcode:
            raise subprocess.CalledProcessError(retcode, process.args, output=stdout, stderr=stderr)
    return subprocess.CompletedProcess(process.args, retcode, stdout, stderr)


def makedirs(path):
    if not os.path.exists(path):
        os.makedirs(path)


def parse_arguments(timestamp):
    parser = argparse.ArgumentParser(description='Get performance stats of AI client.')
    parser.add_argument(
        '--configs',
        default='src/configs',
        help='Path to configs to run performance for (defaults to src/configs).',
        type=str,
        dest='configs',
    )
    parser.add_argument(
        '--levels',
        default='src/levels/custom',
        help='Path to levels to run performance against (defaults to src/levels/custom).',
        type=str,
        dest='levels',
    )
    parser.add_argument(
        '--out',
        default=f'stats/{timestamp.replace(":", "-")}.json',
        help='Path to output file (defaults to stats/{timestamp}.json)',
        type=str,
        dest='out',
    )
    parser.add_argument(
        '--timeout',
        default=None,
        help='Max. time (in seconds) to solve a level. Kills a process if timeout is exceeded. Defaults to no timeout.',
        type=int,
        dest='timeout',
    )
    return parser.parse_args(sys.argv[1:])


if __name__ == '__main__':
    main()
