#!/bin/bash

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

function setup_hyperledger_fabric() {
    if [ ! -d "./network/bin" ] || [ ! -d "./network/builders" ] || [ ! -d "./network/config" ]; then
        echo -e "${YELLOW}Required folders not found in ./network/. Proceeding with setup.${NC}"

        curl -sSLO https://raw.githubusercontent.com/hyperledger/fabric/main/scripts/install-fabric.sh
        chmod +x install-fabric.sh

        ./install-fabric.sh

        cp -r fabric-samples/bin ./network/
        cp -r fabric-samples/builders ./network/
        cp -r fabric-samples/config ./network/

        rm -rf fabric-samples
        rm -f install-fabric.sh

        echo -e "${GREEN}Hyperledger Fabric setup complete.${NC}"
    else
        echo -e "${RED}The required folders already exist in ./network/. No actions needed.${NC}"
    fi
}

function setup_hyperledger_indy() {
    if [ ! -d "$HOME/indy-sdk" ]; then
        echo -e "${YELLOW}The indy-sdk folder does not exist. Proceeding with cloning.${NC}"

        git clone https://github.com/hyperledger/indy-sdk.git

        cd indy-sdk

        docker build -f ci/indy-pool.dockerfile -t indy_pool .
        docker run -itd -p 9701-9708:9701-9708 indy_pool

        cd ..

        echo -e "${GREEN}indy-sdk installation and Docker container setup are complete.${NC}"
    else
        echo -e "${RED}The indy-sdk folder already exists. No actions will be performed.${NC}"
    fi
}

function start_application() {
    echo -e "${YELLOW}Starting node-connection application...${NC}"

    echo -e "${YELLOW}Checking Hyperledger installation...${NC}"
    setup_hyperledger_fabric
    setup_hyperledger_indy

    echo -e "${YELLOW}Starting node-connection network...${NC}"
    ./network/node-connection-network/network.sh up -ca

    # TODO: 백엔드 서버 시작
    
    # TODO: 프론트엔드 서버 시작
    echo -e "${GREEN}Starting node-connection application complete.${NC}"
}

# Function to stop the networkw
function stop_application() {
    echo -e "${YELLOW}Stopping node-connection network...${NC}"
    ./network/node-connection-network/network.sh down

    # TODO: 백엔드 서버 중지

    # TODO: 프론트엔드 서버 중지

    echo -e "${GREEN}Stopping node-connection application complete.${NC}"
}

# Check the first argument passed to the script
if [ "$1" == "start" ]; then
    start_application
  
elif [ "$1" == "stop" ]; then
    stop_application
else
    echo -e "${RED}Invalid argument. Use 'start' to start the application and 'stop' to stop the application.${NC}"
    exit 1
fi
