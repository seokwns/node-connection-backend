#!/bin/bash

function start_application() {
    echo "Starting node-connection network..."
    ./network/node-connection-network/network.sh up -ca

    # TODO: 백엔드 서버 시작
    
    # TODO: 프론트엔드 서버 시작
}

# Function to stop the network
function stop_application() {
    echo "Stopping node-connection network..."
    ./network/node-connection-network/network.sh down

    # TODO: 백엔드 서버 중지

    # TODO: 프론트엔드 서버 중지
}

# Check the first argument passed to the script
if [ "$1" == "start" ]; then
    start_application
  
elif [ "$1" == "stop" ]; then
    stop_application
else
    echo "Invalid argument. Use 'start' to start the application and 'stop' to stop the application."
    exit 1
fi
