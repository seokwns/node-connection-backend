#!/bin/bash

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

function setup_requirements() {
    # Determine the shell type
    SHELL_PROFILE=""
    if [ -n "$BASH_VERSION" ]; then
        SHELL_PROFILE="$HOME/.bashrc"
    elif [ -n "$ZSH_VERSION" ]; then
        SHELL_PROFILE="$HOME/.zshrc"
    else
        echo -e "${RED}Unsupported shell. Please use bash or zsh.${NC}"
        return 1
    fi

    # Check if Go is installed
    if ! command -v go &> /dev/null
    then
        echo -e "${YELLOW}Go is not installed. Installing Go...${NC}"
        wget https://golang.org/dl/go1.19.1.linux-amd64.tar.gz
        sudo tar -xzf go1.19.1.linux-amd64.tar.gz -C /usr/local/
        # Set up Go environment variables
        if ! grep -q "export PATH=\$PATH:/usr/local/go/bin" "$SHELL_PROFILE"; then
            echo "export PATH=\$PATH:/usr/local/go/bin" >> "$SHELL_PROFILE"
            source "$SHELL_PROFILE"
        fi
        echo -e "${GREEN}Go has been installed successfully.${NC}"
    else
        echo -e "${GREEN}Go is already installed.${NC}"
    fi

    # Check if Node.js is installed
    if ! command -v node &> /dev/null
    then
        echo -e "${YELLOW}Node.js is not installed. Installing Node.js...${NC}"
        # Install NVM (Node Version Manager)
        wget -qO- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.4/install.sh | bash
        # Load NVM script
        export NVM_DIR="$HOME/.nvm"
        [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
        
        # Install the latest LTS version of Node.js
        nvm install --lts
        nvm use --lts
        echo -e "${GREEN}Node.js has been installed successfully.${NC}"
    else
        echo -e "${GREEN}Node.js is already installed.${NC}"
    fi
}

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
    ./setOrgEnv.sh Registry | while IFS= read -r line; do
        if [[ $line =~ ^([^=]+)=(.*)$ ]]; then
            var_name=${BASH_REMATCH[1]}
            var_value=${BASH_REMATCH[2]}
            export $var_name="$var_value"
        fi
    done
    ./setOrgEnv.sh Viewer | while IFS= read -r line; do
        if [[ $line =~ ^([^=]+)=(.*)$ ]]; then
            var_name=${BASH_REMATCH[1]}
            var_value=${BASH_REMATCH[2]}
            export $var_name="$var_value"
        fi
    done
    cd ../..
}

function export_config() {
    # Write the configuration to hyperledger.config.sh
    echo '#!/bin/bash' > hyperledger.config.sh
    echo '' >> hyperledger.config.sh
    echo '# Hyperledger Fabric and Indy Configuration' >> hyperledger.config.sh
    echo 'function export_config() {' >> hyperledger.config.sh

    # Indy Wallet Storage
    echo '    # Indy Wallet Storage' >> hyperledger.config.sh
    echo '    USER_WALLET_STORAGE="src/main/java/node/connection/wallet/user"' >> hyperledger.config.sh
    echo '    COURT_WALLET_STORAGE="src/main/java/node/connection/wallet/court"' >> hyperledger.config.sh
    echo '    export USER_WALLET_STORAGE COURT_WALLET_STORAGE' >> hyperledger.config.sh

    # Fabric CA Information
    echo '' >> hyperledger.config.sh
    echo '    # Fabric CA Information' >> hyperledger.config.sh
    echo '    CA_NAME="ca-registry"' >> hyperledger.config.sh
    echo '    CA_URL="http://217.15.165.146:7054"' >> hyperledger.config.sh
    echo '    CA_ADMIN_NAME="admin"' >> hyperledger.config.sh
    echo '    CA_ADMIN_PASSWORD="adminpw"' >> hyperledger.config.sh
    echo '    CA_PEM=$(cat ./network/node-connection-network/organizations/fabric-ca/registry/ca-cert.pem)' >> hyperledger.config.sh
    echo '    export CA_NAME CA_URL CA_ADMIN_NAME CA_ADMIN_PASSWORD CA_PEM' >> hyperledger.config.sh

    # Fabric User Information
    echo '' >> hyperledger.config.sh
    echo '    # Fabric User Information' >> hyperledger.config.sh
    echo '    USER_MSP="RegistryMSP"' >> hyperledger.config.sh
    echo '    USER_AFFILIATION="registry.department1"' >> hyperledger.config.sh
    echo '    export USER_MSP USER_AFFILIATION' >> hyperledger.config.sh

    # Fabric Organization Information
    echo '' >> hyperledger.config.sh
    echo '    # Fabric Organization Information' >> hyperledger.config.sh
    echo '    REGISTRY_PEER_NAME="peer0.registry.node.connection"' >> hyperledger.config.sh
    echo '    REGISTRY_PEER_URL="grpcs://217.15.165.146:7051"' >> hyperledger.config.sh
    echo '    REGISTRY_PEER_PEM=$(cat ./network/node-connection-network/organizations/peerOrganizations/registry.node.connection/tlsca/tlsca.registry.node.connection-cert.pem)' >> hyperledger.config.sh
    echo '    export REGISTRY_PEER_NAME REGISTRY_PEER_URL REGISTRY_PEER_PEM' >> hyperledger.config.sh

    echo '' >> hyperledger.config.sh
    echo '    VIEWER_PEER_NAME="peer0.viewer.node.connection"' >> hyperledger.config.sh
    echo '    VIEWER_PEER_URL="grpcs://217.15.165.146:9051"' >> hyperledger.config.sh
    echo '    VIEWER_PEER_PEM=$(cat ./network/node-connection-network/organizations/peerOrganizations/viewer.node.connection/tlsca/tlsca.viewer.node.connection-cert.pem)' >> hyperledger.config.sh
    echo '    export VIEWER_PEER_NAME VIEWER_PEER_URL VIEWER_PEER_PEM' >> hyperledger.config.sh

    echo '' >> hyperledger.config.sh
    echo '    ORDERER_NAME="orderer.node.connection"' >> hyperledger.config.sh
    echo '    ORDERER_URL="grpcs://217.15.165.146:7050"' >> hyperledger.config.sh
    echo '    ORDERER_PEM=$(cat ./network/node-connection-network/organizations/ordererOrganizations/node.connection/tlsca/tlsca.node.connection-cert.pem)' >> hyperledger.config.sh
    echo '    export ORDERER_NAME ORDERER_URL ORDERER_PEM' >> hyperledger.config.sh

    # Fabric Channel Information
    echo '' >> hyperledger.config.sh
    echo '    # Fabric Channel Information' >> hyperledger.config.sh
    echo '    CHANNEL_NAME="nodeconnectionchannel"' >> hyperledger.config.sh
    echo '    export CHANNEL_NAME' >> hyperledger.config.sh

    echo '}' >> hyperledger.config.sh
    echo '' >> hyperledger.config.sh
    echo '# Call the function to export the configurations' >> hyperledger.config.sh
    echo 'export_config' >> hyperledger.config.sh

    # Make the script executable
    chmod +x hyperledger.config.sh
}

function start_application() {
    echo -e "${YELLOW}Starting node-connection application...${NC}"

    echo -e "${YELLOW}Checking requirements installation...${NC}"
    setup_requirements

    echo -e "${YELLOW}Checking Hyperledger installation...${NC}"
    setup_hyperledger_fabric
    setup_hyperledger_indy

    echo -e "${YELLOW}Starting node-connection network...${NC}"
    ./node-connection-network.sh up

    echo -e "${YELLOW}Config node-connection network...${NC}"
    config_org_env

    echo -e "${YELLOW}Create default channel...${NC}"
    ./network/node-connection-network/network.sh createChannel -c nodeconnectionchannel

    echo -e "${YELLOW}Export hyperledger network config...${NC}"
    export_config
    source ./hyperledger.config.sh

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
