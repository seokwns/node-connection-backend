package node.connection.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection.dto.registry.RegistryDocumentDto;
import node.connection.entity.FabricRegister;
import node.connection.hyperledger.FabricConfig;
import node.connection.hyperledger.fabric.*;
import node.connection.hyperledger.fabric.ca.*;
import node.connection.repository.FabricRegisterRepository;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class FabricService {

    private final FabricConfig fabricConfig;

    private final CAInfo registryCaInfo;

    private final CAInfo viewerCaInfo;

    private final FabricCAConnector registryCAConnector;

    private final FabricCAConnector viewerCAConnector;

    private final Registrar registrar;

    private final Registrar viewerRegistrar;

    private FabricConnector fabricConnector;

    private NetworkConfig networkConfig;

    private Client client;

    private final Channel channel;

    private final ObjectMapper objectMapper;

    private final FabricRegisterRepository fabricRegisterRepository;

    private final static String VIEWER_MSP = "ViewerMSP";

    private final static String REGISTRY_MSP = "RegistryMSP";

    private final static String ID_DELIMITER = ".api.";

    public FabricService(
            @Autowired FabricConfig fabricConfig,
            @Autowired ObjectMapper objectMapper,
            @Autowired FabricRegisterRepository fabricRegisterRepository
    ) {
        this.fabricConfig = fabricConfig;
        this.objectMapper = objectMapper;
        this.fabricRegisterRepository = fabricRegisterRepository;

        this.registryCaInfo = CAInfo.builder()
                .name(this.fabricConfig.getRegistryCaName())
                .url(this.fabricConfig.getRegistryCaUrl())
                .pemFile(this.fabricConfig.getRegistryCaPemFilePath())
                .allowAllHostNames(true)
                .build();

        this.viewerCaInfo = CAInfo.builder()
                .name(this.fabricConfig.getViewerCaName())
                .url(this.fabricConfig.getViewerCaUrl())
                .pemFile(this.fabricConfig.getViewerCaPemFilePath())
                .allowAllHostNames(true)
                .build();

        this.registryCAConnector = new FabricCAConnector(this.registryCaInfo);
        this.viewerCAConnector = new FabricCAConnector(this.viewerCaInfo);

        CAUser admin = CAUser.builder()
                .name(this.fabricConfig.getCaAdminName())
                .secret(this.fabricConfig.getCaAdminSecret())
                .build();

        this.registrar = this.registryCAConnector.registrarEnroll(admin);
        this.viewerRegistrar = this.viewerCAConnector.registrarEnroll(admin);
        String registryRegistrarEnrollment = this.registrar.getEnrollment().serialize(this.objectMapper);
        this.fabricRegisterRepository.save(FabricRegister.of(this.registrar, admin.getSecret(), registryRegistrarEnrollment));
        log.info("fabric-ca admin 계정 enroll 완료");

        String registryId = getId(REGISTRY_MSP, this.fabricConfig.getRootNumber());
        this.registryCAConnector.register(registryId, this.fabricConfig.getRootPassword(), this.registrar);

        Enrollment e = this.registryCAConnector.enroll(registryId, this.fabricConfig.getRootPassword());
        CAEnrollment caEnrollment = CAEnrollment.of(e);
        Client client = Client.builder()
                .name(registryId)
                .mspId(REGISTRY_MSP)
                .enrollment(caEnrollment)
                .build();

        String rootEnrollment = caEnrollment.serialize(objectMapper);
        this.fabricRegisterRepository.save(FabricRegister.of(client, this.fabricConfig.getRootPassword(), rootEnrollment));
        log.info("fabric-ca root 계정 enroll 완료");

        this.initialize();
        log.info("Fabric client 및 네트워크 설정 완료");
        log.debug("client: {}", this.client);

        this.fabricConnector = new FabricConnector(this.client);
        log.info("Fabric Connector 생성 완료");

        this.channel = fabricConnector.connectToChannel(this.networkConfig);
        log.info("채널 연결 완료");
    }

    public void registerToViewerMSP(String phoneNumber, String secret) {
        String id = getId(VIEWER_MSP, phoneNumber);
        String response = this.viewerCAConnector.register(id, secret, this.viewerRegistrar);

        if (response == null) {
            throw new ServerException(ExceptionStatus.ALREADY_CA_REGISTERED);
        }

        Enrollment e = this.viewerCAConnector.enroll(id, secret);
        CAEnrollment caEnrollment = CAEnrollment.of(e);

        Client client = Client.builder()
                .name(id)
                .mspId(VIEWER_MSP)
                .enrollment(caEnrollment)
                .build();

        String enrollment = client.getEnrollment().serialize(objectMapper);
        this.fabricRegisterRepository.save(FabricRegister.of(client, secret, enrollment));
    }

    public void registerToRegistryMSP(String number, String secret) {
        String id = getId(REGISTRY_MSP, number);
        String response = this.registryCAConnector.register(id, secret, this.registrar);
        if (response == null) {
            throw new ServerException(ExceptionStatus.ALREADY_CA_REGISTERED);
        }

        Enrollment e = this.registryCAConnector.enroll(id, secret);
        CAEnrollment caEnrollment = CAEnrollment.of(e);

        Client client = Client.builder()
                .name(id)
                .mspId(REGISTRY_MSP)
                .enrollment(caEnrollment)
                .build();

        String enrollment = client.getEnrollment().serialize(objectMapper);
        this.fabricRegisterRepository.save(FabricRegister.of(client, secret, enrollment));
    }

    private void initialize() {
        String id = getId(this.fabricConfig.getRootMsp(), this.fabricConfig.getRootNumber());
        FabricRegister register = this.fabricRegisterRepository.findById(id)
                .orElseThrow(() -> new ServerException(ExceptionStatus.NO_FABRIC_CA_DATA));

        CAEnrollment enrollment = CAEnrollment.deserialize(this.objectMapper, register.getEnrollment());

        FabricPeer peer0Registry = FabricPeer.builder()
                .name(this.fabricConfig.getRegistryName())
                .url(this.fabricConfig.getRegistryUrl())
                .pemFile(this.fabricConfig.getRegistryPemFilePath())
                .hostnameOverride(this.fabricConfig.getRegistryName())
                .build();

        FabricPeer peer0Viewer = FabricPeer.builder()
                .name(this.fabricConfig.getViewerName())
                .url(this.fabricConfig.getViewerUrl())
                .pemFile(this.fabricConfig.getViewerPemFilePath())
                .hostnameOverride(this.fabricConfig.getViewerName())
                .build();

        FabricPeer orderer = FabricPeer.builder()
                .name(this.fabricConfig.getOrdererName())
                .url(this.fabricConfig.getOrdererUrl())
                .pemFile(this.fabricConfig.getOrdererPemFilePath())
                .hostnameOverride(this.fabricConfig.getOrdererName())
                .build();

        NetworkConfig networkConfig = new NetworkConfig.Builder()
                .channelName(this.fabricConfig.getChannelName())
                .peer(peer0Registry)
                .peer(peer0Viewer)
                .orderer(orderer)
                .build();

        this.client = Client.of(register, enrollment);
        this.networkConfig = networkConfig;
    }

    public FabricConnector getConnectorById(String id) {
        FabricRegister register = this.fabricRegisterRepository.findById(id)
                .orElseThrow(() -> new ServerException(ExceptionStatus.NO_FABRIC_CA_DATA));

        try {
            CAEnrollment enrollment = this.objectMapper.readValue(register.getEnrollment(), CAEnrollment.class);
            return new FabricConnector(Client.of(register, enrollment));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.JSON_PROCESSING_EXCEPTION);
        }
    }

    public void setChaincode(String name, String version) {
        this.fabricConnector.setChaincode(name, version);
    }

    public FabricProposalResponse invoke(String fcn, List<String> params) {
        return this.fabricConnector.invoke(fcn, params);
    }

    public FabricProposalResponse query(String fcn, List<String> params) {
        return this.fabricConnector.query(fcn, params);
    }

    public RegistryDocumentDto getRegistryDocumentById(String id) {
        List<String> params = List.of(id);
        this.fabricConnector.setChaincode("registry", "1.0.0");
        FabricProposalResponse response = this.fabricConnector.query("GetRegistryDocumentByID", params);
        log.debug(String.valueOf(response));

        String payload = response.getPayload();
        try {
            return this.objectMapper.readValue(payload, RegistryDocumentDto.class);
        } catch (JsonProcessingException e) {
            throw new ServerException(ExceptionStatus.JSON_PROCESSING_EXCEPTION);
        }
    }

    public String getId(String msp, String number) {
        return msp + ID_DELIMITER + number;
    }
}
