#!/bin/bash

[ $# -eq 0 ] && { echo "Usage: $0 <java class name> . Eg: $0 WordCounterMap "; exit 1; }

echo "compiling $1.jar"
cd bin
jar cf $1.jar $1.class
echo "convert to dex"
dx --dex --output $1Dex.jar $1.jar
mv $1.jar ..
mv $1Dex.jar ..
echo "done"
