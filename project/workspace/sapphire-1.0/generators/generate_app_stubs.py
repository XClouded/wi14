#!/usr/bin/python

import os, sys
import subprocess
#from apps.microbenchmark import *
#from apps.linpack import *
#from apps.docs import *
from apps.words import *
#from apps.todo import *
#from apps.minnietwitter import *


def run_cmd(cmd):
    print reduce(lambda x, y: x + " " + y, cmd, "")
    p = subprocess.Popen(cmd)
    p.wait()

if __name__ == '__main__':

    try:  
        sapphire_home = os.environ["SAPPHIRE_HOME"]
    except KeyError: 
        print "SAPPHIRE_HOME is not set"
        sys.exit()
   
    cp_app =  sapphire_home + '/example_apps/' + app_name + '/bin/classes.dex'
    cp_sapphire = sapphire_home + '/sapphire/bin/classes.dex'

    cp_java_app =  sapphire_home + '/example_apps/' + app_name + '/bin/classes'
    cp_java_app_libs =  sapphire_home + '/example_apps/' + app_name + '/libs/*'
    cp_java_sapphire = sapphire_home + '/sapphire/bin/classes'
    
    cmd = ['java', '-cp',  cp_java_app + ':' + cp_java_sapphire + ':' + cp_java_app_libs, \
                           'sapphire.compiler.StubGenerator', \
                           sapphire_home + inFolder, package, \
                           sapphire_home + outFolder]

    run_cmd(cmd)
    print "Done!"
