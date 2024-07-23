#!/bin/bash

function start_network() {
    echo "Starting node-connection network..."
    ./network/node-connection-network/network.sh up -ca
}

# Function to stop the network
function stop_network() {
    echo "Stopping node-connection network..."
    ./network/node-connection-network/network.sh down
}

# Check the first argument passed to the script
if [ "$1" == "up" ]; then
    start_network
elif [ "$1" == "down" ]; then
    stop_network
else
    echo "Invalid argument. Use 'up' to start the network and 'down' to stop the network."
    exit 1
fi
