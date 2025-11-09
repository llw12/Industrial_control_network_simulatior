#!/bin/bash

PID_FILE=$1

if [ -z "$PID_FILE" ]; then
    echo "[ERROR] Usage: $0 <pid_file>"
    exit 1
fi

if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null; then
        kill "$PID"
        echo "[INFO] Simulation process $PID stopped."
        rm -f "$PID_FILE"
    else
        echo "[WARN] Process $PID not found."
    fi
else
    echo "[WARN] PID file not found: $PID_FILE"
fi
