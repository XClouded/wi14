#!/usr/bin/python

import os, sys
import subprocess
from time import sleep
import atexit
import signal
from apps.todo import *
#from apps.microbenchmark import *
#from apps.minnietwitter import *
#from apps.docs import *
#from apps.linpack import *
import getpass
import json
import os

ssh_cmd = "ssh"
user_name = "pingyh"
home_dir = "/homes/iws/pingyh/sapphire/scratch/out"
log_folder = "/homes/iws/pingyh/sapphire/logs"
config_file = "config.json"
app_starter = "sapphire.runtime.SapphireActivityStarter"

cp_dex_app =  home_dir + '/apps/' + app_name + '/bin/classes.dex'
cp_dex_sapphire = home_dir + '/sapphire/bin/classes.dex'
cp_dex_contrib = "$(echo "+ home_dir + "/contrib/voldemort/*.jar | tr ' ' ':')"
p_log = home_dir + '/sapphire/logging.properties'

cp_java_app =  home_dir + '/apps/' + app_name + '/bin/classes'
cp_java_sapphire = home_dir + '/sapphire/bin/classes'
cp_java_contrib = home_dir + "/contrib/*:" + home_dir + "/contrib/javalib/:" + "$(echo "+ home_dir + "/contrib/voldemort-java/*.jar | tr ' ' ':')"


def run_cmd(cmd):
    print reduce(lambda x, y: x + " " + y, cmd, "")
    p = subprocess.Popen(cmd)
    p.wait()

def run_async_cmd(cmd):
    print reduce(lambda x, y: x + " " + y, cmd, "")
    p = subprocess.Popen(cmd)

def parse_config_file(server_file):
    f = open(server_file,"r")
    config = json.JSONDecoder().decode(f.read())
    return config

def copy_classes(nodes, android_home):
    cmd = ["mkdir", "-p"]
    cmd += [home_dir + "/apps/" + app_name + "/bin"]
    run_cmd(cmd)

    # copy java classes
    cmd = ["cp", "-r"]
    cmd += [android_home + "/example_apps/" + app_name + "/bin/classes"]
    cmd += [home_dir + "/apps/" + app_name + "/bin"]
    run_cmd(cmd)

    # copy dex versions
    cmd = ["cp"]
    cmd += [android_home + "/example_apps/" + app_name + "/bin/classes.dex"]
    cmd += [home_dir + "/apps/" + app_name + "/bin"]
    run_cmd(cmd)

    for node in nodes:
        # Copy java classes
        cmd = ["scp", "-r"]
        cmd += [android_home + "/sapphire/bin/classes"]
        cmd += [user_name+"@"+node + ":" + home_dir + "/sapphire/bin/"]
        run_cmd(cmd)
        
        # copy dex versions
        cmd = ["scp", "-r"]
        cmd += [android_home + "/sapphire/bin/classes.dex"]
        cmd += [user_name+"@"+node + ":" + home_dir + "/sapphire/bin/"]
        run_cmd(cmd)

        cmd = ["scp", "-r"]
        cmd += [home_dir + "/apps/" + app_name]
        cmd += [user_name+"@"+node + ":" + home_dir + "/apps/"]
        run_cmd(cmd)
        
        cmd = ["scp", "-r"]
        cmd += [android_home + "/deployment/" + config_file]
        cmd += [user_name+"@"+node + ":" + home_dir + "/"]
        run_cmd(cmd)

def start_oms(oms):
    hostname = oms["hostname"]
    port = oms["port"]

    print 'Starting OMS on '+hostname+":"+port
    cmd = [ssh_cmd, user_name+"@"+hostname]
    # cmd += [home_dir + '/host/linux-x86/bin/dalvik']
    cmd += ['java']
    cmd += ['-Djava.util.logging.config.file=\"' + p_log + '\"']
    cmd += ['-cp ' + cp_java_app + ':' + cp_java_sapphire]
    cmd += ['sapphire.oms.OMSServerImpl', hostname, port]
    cmd += [app_class]
    cmd += [">", log_folder+"/oms-log."+hostname+"."+port, "2>&1 &"]
    run_cmd(cmd)

    sleep(2)

def start_dalvik_oms(oms):
    hostname = oms["hostname"]
    port = oms["port"]

    print 'Starting OMS on '+hostname+":"+port
    cmd = [ssh_cmd, hostname]
    cmd += [home_dir + '/host/linux-x86/bin/dalvik']
    #cmd += ['java']
    cmd += ['-Djava.util.logging.config.file=\"' + p_log + '\"']
    cmd += ['-cp ' + cp_dex_app + ':' + cp_dex_sapphire]
    cmd += ['sapphire.oms.OMSServerImpl', hostname, port]
    cmd += [app_class]
    cmd += [">", log_folder+"/oms-log."+hostname+"."+port, "2>&1 &"]
    run_cmd(cmd)

    sleep(2)

def start_dalvik_servers(servers):
    for s in servers:
        hostname = s["hostname"]
        port = s["port"]
        print "Starting kernel server on "+hostname+":"+port

        # /bin/classes.dex is generated after you try to run an android app from eclipse_adt
        cmd = [ssh_cmd, hostname]
        cmd += [home_dir + '/host/linux-x86/bin/dalvik']
        cmd += ['-Djava.util.logging.config.file=\"' + p_log + '\"']
        cmd += ['-cp ' + cp_dex_app + ':' + cp_dex_sapphire + ':' + cp_dex_contrib]
        cmd += ['sapphire.kernel.server.KernelServerImpl']
        cmd += [hostname, port, home_dir + "/" + config_file]
        cmd += [">", log_folder+"/log."+hostname+"."+port, "2>&1 &"]
        run_cmd(cmd)
    
    sleep(1)

def start_servers(servers):
    for s in servers:
        hostname = s["hostname"]
        port = s["port"]
        print "Starting kernel server on "+hostname+":"+port

        # /bin/classes.dex is generated after you try to run an android app from eclipse_adt
        cmd = [ssh_cmd, user_name+"@"+hostname]
        # cmd += [home_dir + '/host/linux-x86/bin/dalvik']
        cmd += ['java']
        cmd += ['-Djava.util.logging.config.file=\"' + p_log + '\"']
        #cmd += ['-Djava.rmi.server.logCalls=true']
        cmd += ['-cp ' + cp_java_app +':' + '\"' + cp_java_app + '/*\"' + ':' + cp_java_sapphire + ':' + cp_java_contrib]
        cmd += ['sapphire.kernel.server.KernelServerImpl']
        cmd += [hostname, port, home_dir + "/" + config_file]
        cmd += [">", log_folder+"/log."+hostname+"."+port, "2>&1 &"]
        run_cmd(cmd)
    
    sleep(1)


def start_dalvik_clients(clients):
    for client in clients:
        hostname = client["hostname"]
        port = client["port"]
        print 'Starting App on '+hostname+":"+port

        # /bin/classes.dex is generated after you try to run an android app from eclipse_adt
        cmd = [ssh_cmd, hostname]
        cmd += [home_dir + '/host/linux-x86/bin/dalvik']
        cmd += ['-Djava.util.logging.config.file=\"' + p_log + '\"']
        cmd += ['-cp ' + cp_dex_app + ':' + cp_dex_sapphire + ':' + cp_dex_contrib]
        cmd += [app_starter, hostname, port, home_dir + "/" + config_file, app_client]
        cmd += [">", log_folder+"/client-log."+hostname+"."+port, "2>&1 &"]
        run_async_cmd(cmd)

def start_clients(clients):
    for client in clients:
        hostname = client["hostname"]
        port = client["port"]
        print 'Starting App on '+hostname+":"+port

        # /bin/classes.dex is generated after you try to run an android app from eclipse_adt
        cmd = [ssh_cmd, user_name+"@"+hostname]
        cmd += ['java']
        cmd += ['-Djava.util.logging.config.file=\"' + p_log + '\"']
        cmd += ['-cp ' + cp_java_app +':' + '\"' + cp_java_app + '/*\"' + ':' + cp_java_sapphire + ':' + cp_java_contrib]
        cmd += [app_starter, hostname, port, home_dir + "/" + config_file, app_client]
        cmd += [">", log_folder+"/client-log."+hostname+"."+port, "2>&1 &"]
        run_async_cmd(cmd)

if __name__ == '__main__':
    try:  
        sapphire_home = os.environ["SAPPHIRE_HOME"]
    except KeyError: 
        print "SAPPHIRE_HOME is not set"
        sys.exit()

    # check whether we passed in a config file
    if len(sys.argv) > 1:
        config_file = os.path.basename(sys.argv[1])
    config = parse_config_file(config_file) 
    
    # Copy the app & sapphire dex classes on the nodes
    copy_classes(config["nodes"], sapphire_home)

    start_oms(config["oms"])
    start_servers(config["servers"])
    start_clients(config["clients"])
    
