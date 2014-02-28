#!/usr/bin/python

import os, sys
import subprocess


def run_cmd(cmd):
    print reduce(lambda x, y: x + " " + y, cmd, "")
    p = subprocess.Popen(cmd)
    p.wait()

if __name__ == '__main__':

    try:  
        sapphire_home = os.environ["SAPPHIRE_HOME"]
    except KeyError: 
        print "SAPPHIRE_HOME is not set - should have been set while building android"
        sys.exit()
   
    inFolder = sapphire_home + '/sapphire/bin/classes/sapphire/policy/'
    package = 'sapphire.policy'
    outFolder = sapphire_home + '/sapphire/src/sapphire/policy/stubs/'
    
    cp_sapphire = sapphire_home + '/sapphire/bin/classes.dex'
    cp_java_sapphire = sapphire_home + '/sapphire/bin/classes'
 
    #cmd = [android_home + '/out/host/linux-x86/bin/dalvik', '-cp', cp_sapphire, 'sapphire.compiler.StubGenerator', inFolder, package, outFolder]
    cmd = ['java', '-cp', cp_java_sapphire, 'sapphire.compiler.StubGenerator', inFolder, package, outFolder]

    run_cmd(cmd)
    print "Done!"
