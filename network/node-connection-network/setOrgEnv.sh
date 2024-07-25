#!/bin/bash
#
# SPDX-License-Identifier: Apache-2.0




# default to using Org1
ORG=${1:-Org1}

# Exit on first error, print all commands.
set -e
set -o pipefail

# Where am I?
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

echo "${DIR}"

ORDERER_CA=${DIR}/node-connection-network/organizations/ordererOrganizations/node.connection/tlsca/tlsca.node.connection-cert.pem
PEER0_ORG1_CA=${DIR}/node-connection-network/organizations/peerOrganizations/org1.node.connection/tlsca/tlsca.org1.node.connection-cert.pem
PEER0_ORG2_CA=${DIR}/node-connection-network/organizations/peerOrganizations/org2.node.connection/tlsca/tlsca.org2.node.connection-cert.pem
PEER0_ORG3_CA=${DIR}/node-connection-network/organizations/peerOrganizations/org3.node.connection/tlsca/tlsca.org3.node.connection-cert.pem

ORG_LOWER=$(echo "${ORG}" | tr '[:upper:]' '[:lower:]')

if [[ ${ORG_LOWER} == "org1" || ${ORG_LOWER} == "digibank" ]]; then

   CORE_PEER_LOCALMSPID=Org1MSP
   CORE_PEER_MSPCONFIGPATH=${DIR}/node-connection-network/organizations/peerOrganizations/org1.node.connection/users/Admin@org1.node.connection/msp
   CORE_PEER_ADDRESS=localhost:7051
   CORE_PEER_TLS_ROOTCERT_FILE=${DIR}/node-connection-network/organizations/peerOrganizations/org1.node.connection/tlsca/tlsca.org1.node.connection-cert.pem

if [[ ${ORG_LOWER} == "org2" || ${ORG_LOWER} == "digibank" ]]; then

   CORE_PEER_LOCALMSPID=Org2MSP
   CORE_PEER_MSPCONFIGPATH=${DIR}/node-connection-network/organizations/peerOrganizations/org2.node.connection/users/Admin@org2.node.connection/msp
   CORE_PEER_ADDRESS=localhost:9051
   CORE_PEER_TLS_ROOTCERT_FILE=${DIR}/node-connection-network/organizations/peerOrganizations/org2.node.connection/tlsca/tlsca.org2.node.connection-cert.pem

else
   echo "Unknown \"$ORG\", please choose Org1/Digibank or Org2/Magnetocorp"
   echo "For example to get the environment variables to set upa Org2 shell environment run:  ./setOrgEnv.sh Org2"
   echo
   echo "This can be automated to set them as well with:"
   echo
   echo 'export $(./setOrgEnv.sh Org2 | xargs)'
   exit 1
fi

# output the variables that need to be set
echo "CORE_PEER_TLS_ENABLED=true"
echo "ORDERER_CA=${ORDERER_CA}"
echo "PEER0_ORG1_CA=${PEER0_ORG1_CA}"
echo "PEER0_ORG2_CA=${PEER0_ORG2_CA}"
echo "PEER0_ORG3_CA=${PEER0_ORG3_CA}"

echo "CORE_PEER_MSPCONFIGPATH=${CORE_PEER_MSPCONFIGPATH}"
echo "CORE_PEER_ADDRESS=${CORE_PEER_ADDRESS}"
echo "CORE_PEER_TLS_ROOTCERT_FILE=${CORE_PEER_TLS_ROOTCERT_FILE}"

echo "CORE_PEER_LOCALMSPID=${CORE_PEER_LOCALMSPID}"
