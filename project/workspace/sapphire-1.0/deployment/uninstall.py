#!/usr/bin/python

import os, sys
import subprocess
from time import sleep
import atexit
import signal
import getpass
import json
import os

nodes_file = "config.json"
home_dir = "/scratch/" + getpass.getuser()

def run_cmd(cmd, sout=None):
    print reduce(lambda x, y: x + " " + y, cmd, "")
    p = subprocess.Popen(cmd, stdout = sout)
    p.wait()

def parse_server_config(server_file):
    f = open(server_file,"r")
    config = json.JSONDecoder().decode(f.read())
    return config

def remove_out(nodes):

    # Cleanup out locally
    cmd = ["rm", "-fr"]
    cmd += [home_dir + "/out"]
    run_cmd(cmd)
    
    for node in nodes:
        cmd = ["ssh", node]
        cmd += ["rm", "-fr"]
        cmd += [home_dir + "/out"]
        run_cmd(cmd)

if __name__ == '__main__':
    try:  
        sapphire_home = os.environ["SAPPHIRE_HOME"]
    except KeyError: 
        print "SAPPHIRE_HOME is not set"
        sys.exit()

    config = parse_server_config(nodes_file)
    remove_out(config["nodes"]) 
