#!/bin/bash
PID=`cat $1`
LOG_FILE="$2"

top -b -d 1 -p $PID | awk \
    -v pid="$PID" -v cpuLog="$LOG_FILE" '
    $1+0>0 {printf "%d\n", \
            $9 > cpuLog
            fflush(cpuLog)
            close(cpuLog)}'
