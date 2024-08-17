package node.connection.hyperledger;

import java.io.File;

public class FabricConfig {
    public static String mspFolder = "./msp";
    public static String tlsFolder = "./tls";

    public static String caName = "ca-registry";
    public static String caUrl = "https://217.15.165.146:7054";
    public static String caPemFile = "ca-registry.pem";
    public static String caPemFilePath = FabricConfig.tlsFolder + File.separator  + FabricConfig.caPemFile;

    public static String caAdminName = "admin";
    public static String caAdminSecret = "adminpw";

    public static String userMspId = "RegistryMSP";
    public static String userAffiliation = "org1.department1";

    public static String user1MspName = "user1.json";
    public static String user1MspPath = FabricConfig.mspFolder + File.separator + FabricConfig.user1MspName;

    public static String peer0RegistryName = "peer0.registry.node.connection";
    public static String peer0RegistryPemFile = "peer0-registry.pem";
    public static String peer0RegistryPemFilePath = FabricConfig.tlsFolder + File.separator + FabricConfig.peer0RegistryPemFile;
    public static String peer0RegistryUrl = "grpcs://217.15.165.146:7051";

    public static String peer0ViewerName = "peer0.viewer.node.connection";
    public static String peer0ViewerPemFile = "peer0-viewer.pem";
    public static String peer0ViewerPemFilePath = FabricConfig.tlsFolder + File.separator + FabricConfig.peer0ViewerPemFile;
    public static String peer0ViewerUrl = "grpcs://217.15.165.146:9051";

    public static String ordererName = "orderer.node.connection";
    public static String ordererUrl = "grpcs://217.15.165.146:7050";
    public static String ordererPemFile = "orderer.pem";
    public static String ordererPemFilePath = FabricConfig.tlsFolder + File.separator + FabricConfig.ordererPemFile;

    public static String channelName = "nodeconnectionchannel";
}
