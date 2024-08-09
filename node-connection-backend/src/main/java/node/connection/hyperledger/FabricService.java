package node.connection.hyperledger;

import lombok.extern.slf4j.Slf4j;
import node.connection.hyperledger.fabric.Client;
import node.connection.hyperledger.fabric.FabricConnector;
import node.connection.hyperledger.fabric.FabricPeer;
import node.connection.hyperledger.fabric.NetworkConfig;
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

    private NetworkConfig networkConfig;

    private Client client;

    private Channel channel;


    public FabricService() {
        this.caInfo = new CAInfo.Builder()
                .name(TestConfig.caName)
                .url(TestConfig.caUrl)
                .pemFile(TestConfig.caPemFilePath)
                .allowAllHostNames(true)
                .build();

        this.fabricCAConnector = new FabricCAConnector(this.caInfo);
        HFCAInfo info = this.fabricCAConnector.info();
        log.debug("caName:{}, version:{}", info.getCAName(), info.getVersion());

        CAUser admin = CAUser.builder()
                .name(TestConfig.caAdminName)
                .secret(TestConfig.caAdminSecret)
                .build();

        this.registrar = this.fabricCAConnector.registrarEnroll(admin);
        String adminJson = registrar.toJson();
        FileUtils.write(TestConfig.mspFolder + "/ca-admin.json", adminJson);
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

        String mspId = TestConfig.userMspId;
        String affiliation = TestConfig.userAffiliation;
        Client client = fabricCAConnector.register(mspId, affiliation, this.registrar);
        String clientJson = client.toJson();
        FileUtils.write(TestConfig.user1MspPath, clientJson);
    }

    private void initialize() {
        String userJson = FileUtils.read(TestConfig.user1MspPath);

        FabricPeer peer0Org1 = FabricPeer.builder()
                .name(TestConfig.peer0Org1Name)
                .url(TestConfig.peer0Org1Url)
                .pemFile(TestConfig.peer0Org1PemFilePath)
                .hostnameOverride(TestConfig.peer0Org1Name)
                .build();

        FabricPeer peer0Org2 = FabricPeer.builder()
                .name(TestConfig.peer0Org2Name)
                .url(TestConfig.peer0Org2Url)
                .pemFile(TestConfig.peer0Org2PemFilePath)
                .hostnameOverride(TestConfig.peer0Org2Name)
                .build();

        FabricPeer orderer = FabricPeer.builder()
                .name(TestConfig.ordererName)
                .url(TestConfig.ordererUrl)
                .pemFile(TestConfig.ordererPemFilePath)
                .hostnameOverride(TestConfig.ordererName)
                .build();

        NetworkConfig networkConfig = new NetworkConfig.Builder()
                .channelName(TestConfig.channelName)
                .peer(peer0Org1)
                .peer(peer0Org2)
                .orderer(orderer)
                .build();

        this.client = Client.fromJson(userJson);
        this.networkConfig = networkConfig;
    }
}
