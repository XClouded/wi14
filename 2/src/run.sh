#!/bin/sh

xterm -geometry 70x12+0+0 -e     "java LockClient 9000; bash" &
xterm -geometry 70x12+425+0 -e   "java LockClient 9001; bash" & 
xterm -geometry 70x18+0+180 -e   "java PaxosNode 9002; bash"  &
xterm -geometry 70x18+425+180 -e "java PaxosNode 9003; bash"  &
xterm -geometry 70x18+850+180 -e "java PaxosNode 9004; bash"  &
xterm -geometry 70x18+0+450 -e   "java PaxosNode 9005; bash"  &
xterm -geometry 70x18+425+450 -e "java PaxosNode 9006; bash"
