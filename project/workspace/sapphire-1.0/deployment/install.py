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


def create_out(sapphire_home):

    print 'Preparing the out folder...'

    # Cleanup out
    cmd = ["rm", "-fr"]
    cmd += [home_dir + "/out"]
    run_cmd(cmd)

    # Create the directory structure
    cmd = ["mkdir", "-p"]
    cmd += [home_dir + "/out/host/linux-x86/bin"]
    run_cmd(cmd)

    cmd = ["mkdir", "-p"]
    cmd += [home_dir + "/out/host/linux-x86/framework"]
    run_cmd(cmd)
    
    cmd = ["mkdir", "-p"]
    cmd += [home_dir + "/out/sapphire/lib"]
    run_cmd(cmd)
    
    cmd = ["mkdir", "-p"]
    cmd += [home_dir + "/out/sapphire/bin"]
    run_cmd(cmd)
    
    cmd = ["mkdir", "-p"]
    cmd += [home_dir + "/out/logs"]
    run_cmd(cmd)

    cmd = ["mkdir", "-p"]
    cmd += [home_dir + "/out/apps"]
    run_cmd(cmd)

    # Copy necessary folders
    cmd = ["cp", "-r"]
    cmd += [sapphire_home + "/out/host/linux-x86/lib"]
    cmd += [home_dir + "/out/host/linux-x86/"]
    run_cmd(cmd)

    cmd = ["cp", "-r"]
    cmd += [sapphire_home + "/out/host/linux-x86/usr"]
    cmd += [home_dir + "/out/host/linux-x86/"]
    run_cmd(cmd)
    
    # Copy the necessary files from bin
    ## Replace whoami in dalvik
    cmd = ["sed"]
    cmd += ["s/$$$whoami/" + getpass.getuser() + "/g"]
    cmd += [sapphire_home + "/deployment/dalvik"]
    dalvik_file = open(home_dir + "/out/host/linux-x86/bin/dalvik", "w")
    run_cmd(cmd, sout = dalvik_file)
    dalvik_file.close()

    cmd = ["chmod", "+x"]
    cmd += [home_dir + "/out/host/linux-x86/bin/dalvik"]
    run_cmd(cmd)

    cmd = ["cp", "-r"]
    cmd += [sapphire_home + "/out/host/linux-x86/bin/dalvikvm"]
    cmd += [home_dir + "/out/host/linux-x86/bin/"]
    run_cmd(cmd)

    cmd = ["cp", "-r"]
    cmd += [sapphire_home + "/out/host/linux-x86/bin/dexopt"]
    cmd += [home_dir + "/out/host/linux-x86/bin/"]
    run_cmd(cmd)

    # Copy the necessary files from framework
    cmd = ["cp", "-r"]
    cmd += [sapphire_home + "/out/host/linux-x86/framework/apache-xml-hostdex.jar"]
    cmd += [home_dir + "/out/host/linux-x86/framework/"]
    run_cmd(cmd)

    cmd = ["cp", "-r"]
    cmd += [sapphire_home + "/out/host/linux-x86/framework/bouncycastle-hostdex.jar"]
    cmd += [home_dir + "/out/host/linux-x86/framework/"]
    run_cmd(cmd)

    cmd = ["cp", "-r"]
    cmd += [sapphire_home + "/out/host/linux-x86/framework/core-hostdex.jar"]
    cmd += [home_dir + "/out/host/linux-x86/framework/"]
    run_cmd(cmd)

    # Copy log properties
    cmd = ["cp", "-r"]
    cmd += [sapphire_home + "/sapphire/logging.properties"]
    cmd += [home_dir + "/out/sapphire/"]
    run_cmd(cmd)

    # copy java classes to contrib
    # create the correct directory structure
    cmd = ["mkdir", "-p"]
    cmd += [home_dir + "/out/contrib/javalib/org/"]
    run_cmd(cmd)
    # copy java class files
    cmd = ["cp", "-r"]
    cmd += [sapphire_home + "/out/target/common/obj/JAVA_LIBRARIES/core_intermediates/classes/org/json"]
    cmd += [home_dir + "/out/contrib/javalib/org/"]
    run_cmd(cmd)
    # Copy contrib jars
    cmd = ["cp", "-r"]
    cmd += [sapphire_home + "/contrib"]
    cmd += [home_dir + "/out/"]
    run_cmd(cmd)

    # Create archive
    #cmd = ["tar", "-czvf"]
    #cmd += [home_dir + "/out.tar.gz"]
    #cmd += [home_dir + "/out"]
    #run_cmd(cmd)

def copy_out_on_nodes(nodes):
    print "Copying out on all the nodes"
    devnull = open('/dev/null', 'w')

    for node in nodes:
        print "Copying on " + node
        cmd = ["scp", "-r"]
        cmd += [home_dir + "/out"]
        cmd += [node + ":" + home_dir]
        run_cmd(cmd, sout=devnull)

        # It takes longer to archive and then dearchive
        #cmd = ["ssh", node]
        #cmd += ["tar", "-xvf"]
        #cmd += [home_dir + "/out.tar.gz"]
        #run_cmd(cmd)

if __name__ == '__main__':
    try:  
        sapphire_home = os.environ["SAPPHIRE_HOME"]
    except KeyError: 
        print "SAPPHIRE_HOME is not set"
        sys.exit()

    if len(sys.argv) > 1:
        nodes_file = os.path.basename(sys.argv[1])
    config = parse_server_config(nodes_file)
    create_out(sapphire_home)
    copy_out_on_nodes(config["nodes"]) 
