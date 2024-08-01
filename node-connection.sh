#!/bin/bash

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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
        echo -e "${BLUE}The required folders already exist in ./network/. No actions needed.${NC}"
    fi
}

function setup_hyperledger_indy() {
    if [ ! -d "./indy-sdk" ]; then
        echo -e "${YELLOW}The indy-sdk folder does not exist. Proceeding with cloning.${NC}"

        git clone https://github.com/hyperledger/indy-sdk.git

        echo -e "${GREEN}indy-sdk installation and Docker container setup are complete.${NC}"
    else
        echo -e "${BLUE}The indy-sdk folder already exists. No actions will be performed.${NC}"
    fi
}

function config_org_env() {
    cd ./network/node-connection-network
    export PATH=$PATH:$(realpath ../bin)
    export FABRIC_CFG_PATH=$(realpath ../config)
    ./setOrgEnv.sh Org1 | while IFS= read -r line; do
        if [[ $line =~ ^([^=]+)=(.*)$ ]]; then
            var_name=${BASH_REMATCH[1]}
            var_value=${BASH_REMATCH[2]}
            export $var_name="$var_value"
        fi
    done
    ./setOrgEnv.sh Org2 | while IFS= read -r line; do
        if [[ $line =~ ^([^=]+)=(.*)$ ]]; then
            var_name=${BASH_REMATCH[1]}
            var_value=${BASH_REMATCH[2]}
            export $var_name="$var_value"
        fi
    done
    cd ../..
}

function start_application() {
    echo -e "${YELLOW}Starting node-connection application...${NC}"

    echo -e "${YELLOW}Checking Hyperledger installation...${NC}"
    setup_hyperledger_fabric
    setup_hyperledger_indy

    echo -e "${YELLOW}Starting node-connection network...${NC}"
    ./node-connection-network.sh up

    echo -e "${YELLOW}Config node-connection network...${NC}"
    config_org_env

    echo -e "${YELLOW}Create default channel...${NC}"
    ./network/node-connection-network/network.sh createChannel -c nodeconnectionchannel

    # TODO: 백엔드 서버 시작
    
    # TODO: 프론트엔드 서버 시작
    echo -e "${GREEN}Starting node-connection application complete.${NC}"
}

function stop_application() {
    echo -e "${YELLOW}Stopping node-connection network...${NC}"
    ./node-connection-network.sh down

    # TODO: 백엔드 서버 중지

    # TODO: 프론트엔드 서버 중지

    echo -e "${GREEN}Stopping node-connection application complete.${NC}"
}

if [ "$1" == "start" ]; then
    start_application
  
elif [ "$1" == "stop" ]; then
    stop_application
else
    echo -e "${RED}Invalid argument. Use 'start' to start the application and 'stop' to stop the application.${NC}"
    exit 1
fi
