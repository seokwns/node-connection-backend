#!/bin/bash

set -e

function setup_hyperledger() {
    if [ ! -d "./network/bin" ] || [ ! -d "./network/builders" ] || [ ! -d "./network/config" ]; then
        echo "Required folders not found in ./network/. Proceeding with setup."

        curl -sSLO https://raw.githubusercontent.com/hyperledger/fabric/main/scripts/install-fabric.sh
        chmod +x install-fabric.sh

        ./install-fabric.sh

        cp -r fabric-samples/bin ./network/
        cp -r fabric-samples/builders ./network/
        cp -r fabric-samples/config ./network/

        rm -rf fabric-samples
        rm -f install-fabric.sh

        echo "Hyperledger Fabric setup complete."
    else
        echo "The required folders already exist in ./network/. No actions needed."
    fi
}

function start_application() {
    setup_hyperledger

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
