package node.connection.hyperledger;

import java.io.File;

public class TestConfig {
    public static String mspFolder = "./msp";
    public static String tlsFolder = "./tls";

    public static String caName = "ca-org1";
    public static String caUrl = "https://localhost:7054";
    public static String caPemFile = "ca-org1.pem";
    public static String caPemFilePath = TestConfig.tlsFolder + File.separator  + TestConfig.caPemFile;

    public static String caAdminName = "admin";
    public static String caAdminSecret = "adminpw";

    public static String userMspId = "Org1MSP";
    public static String userAffiliation = "org1.department1";

    public static String user1MspName = "user1.json";
    public static String user1MspPath = TestConfig.mspFolder + File.separator + TestConfig.user1MspName;

    public static String peer0Org1Name = "peer0.org1.node.connection";
    public static String peer0Org1PemFile = "peer0-org1.pem";
    public static String peer0Org1PemFilePath = TestConfig.tlsFolder + File.separator + TestConfig.peer0Org1PemFile;
    public static String peer0Org1Url = "grpcs://localhost:7051";

    public static String peer0Org2Name = "peer0.org2.node.connection";
    public static String peer0Org2PemFile = "peer0-org2.pem";
    public static String peer0Org2PemFilePath = TestConfig.tlsFolder + File.separator + TestConfig.peer0Org2PemFile;
    public static String peer0Org2Url = "grpcs://localhost:9051";

    public static String ordererName = "orderer.node.connection";
    public static String ordererUrl = "grpcs://localhost:7050";
    public static String ordererPemFile = "orderer.pem";
    public static String ordererPemFilePath = TestConfig.tlsFolder + File.separator + TestConfig.ordererPemFile;

    public static String channelName = "nodeconnectionchannel";
}
