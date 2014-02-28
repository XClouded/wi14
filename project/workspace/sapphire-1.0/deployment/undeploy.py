#!/usr/bin/python

import os, sys
import subprocess
from time import sleep
import atexit
import signal
import json

ssh_cmd = "ssh"

def parse_server_config(server_file):
    f = open(server_file,"r")
    config = json.JSONDecoder().decode(f.read())
    return config

def run_cmd(cmd):
    print reduce(lambda x, y: x + " " + y, cmd, "")
    return subprocess.Popen(cmd)

def kill_servers(nodes):
    for node in nodes:
        cmd = [ssh_cmd, node]
        cmd += ["pkill dalvikvm"]        
        run_cmd(cmd)
        cmd = [ssh_cmd, node]
        cmd += ["pkill java"]
        run_cmd(cmd)

if __name__ == '__main__':
    if len(sys.argv) > 1:
        server_file = os.path.basename(sys.argv[1])
    else:
        server_file = "config.json"
    config = parse_server_config(server_file)

    kill_servers(config["nodes"])
