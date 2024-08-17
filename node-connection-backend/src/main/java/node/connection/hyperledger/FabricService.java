package node.connection.hyperledger;

import lombok.extern.slf4j.Slf4j;
import node.connection.hyperledger.fabric.Client;
import node.connection.hyperledger.fabric.FabricConnector;
import node.connection.hyperledger.fabric.FabricPeer;
import node.connection.hyperledger.fabric.ca.CAInfo;
import node.connection.hyperledger.fabric.ca.CAUser;
import node.connection.hyperledger.fabric.ca.FabricCAConnector;
import node.connection.hyperledger.fabric.ca.Registrar;
import node.connection.hyperledger.fabric.util.FileUtils;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric_ca.sdk.HFCAInfo;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FabricService {

    private final CAInfo caInfo;

    private final FabricCAConnector fabricCAConnector;

    private final Registrar registrar;

    private FabricConnector fabricConnector;

    private node.connection.hyperledger.fabric.NetworkConfig networkConfig;

    private Client client;

    private Channel channel;


    public FabricService() {
        this.caInfo = new CAInfo.Builder()
                .name(FabricConfig.caName)
                .url(FabricConfig.caUrl)
                .pemFile(FabricConfig.caPemFilePath)
                .allowAllHostNames(true)
                .build();

        this.fabricCAConnector = new FabricCAConnector(this.caInfo);
        HFCAInfo info = this.fabricCAConnector.info();
        log.debug("caName:{}, version:{}", info.getCAName(), info.getVersion());

        CAUser admin = CAUser.builder()
                .name(FabricConfig.caAdminName)
                .secret(FabricConfig.caAdminSecret)
                .build();

        this.registrar = this.fabricCAConnector.registrarEnroll(admin);
        String adminJson = registrar.toJson();
        FileUtils.write(FabricConfig.mspFolder + "/ca-admin.json", adminJson);
        log.info("fabric-ca admin 계정 enroll 완료");

        this.register();
        log.info("fabric-ca user 계정 enroll 완료");

        this.initialize();
        log.info("Fabric client 및 네트워크 설정 완료");
        log.debug("client: {}", this.client);

        this.fabricConnector = new FabricConnector(this.client);
        log.info("Fabric Connector 생성 완료");

        this.channel = fabricConnector.connectToChannel(this.networkConfig);
        log.info("채널 연결 완료");
    }

    public void register() {
        log.info("ca info: {}", this.fabricCAConnector.info());

        String mspId = FabricConfig.userMspId;
        String affiliation = FabricConfig.userAffiliation;
        Client client = fabricCAConnector.register(mspId, affiliation, this.registrar);
        String clientJson = client.toJson();
        FileUtils.write(FabricConfig.user1MspPath, clientJson);
    }

    private void initialize() {
        String userJson = FileUtils.read(FabricConfig.user1MspPath);

        FabricPeer peer0Registry = FabricPeer.builder()
                .name(FabricConfig.peer0RegistryName)
                .url(FabricConfig.peer0RegistryUrl)
                .pemFile(FabricConfig.peer0RegistryPemFilePath)
                .hostnameOverride(FabricConfig.peer0RegistryName)
                .build();

        FabricPeer peer0Viewer = FabricPeer.builder()
                .name(FabricConfig.peer0ViewerName)
                .url(FabricConfig.peer0ViewerUrl)
                .pemFile(FabricConfig.peer0ViewerPemFilePath)
                .hostnameOverride(FabricConfig.peer0ViewerName)
                .build();

        FabricPeer orderer = FabricPeer.builder()
                .name(FabricConfig.ordererName)
                .url(FabricConfig.ordererUrl)
                .pemFile(FabricConfig.ordererPemFilePath)
                .hostnameOverride(FabricConfig.ordererName)
                .build();

        node.connection.hyperledger.fabric.NetworkConfig networkConfig = new node.connection.hyperledger.fabric.NetworkConfig.Builder()
                .channelName(FabricConfig.channelName)
                .peer(peer0Registry)
                .peer(peer0Viewer)
                .orderer(orderer)
                .build();

        this.client = Client.fromJson(userJson);
        this.networkConfig = networkConfig;
    }
}
