package node.connection.hyperledger;

import lombok.Getter;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
@Getter
public class FabricConfig {
    private String mspFolder = "./msp";
    private String tlsFolder = "./tls";

    @Value("${hyperledger.fabric.ca.name}")
    private String caName;

    @Value("${hyperledger.fabric.ca.url}")
    private String caUrl;

    @Value("${hyperledger.fabric.ca.pem}")
    private String caPem;

    private String caPemFile = "ca-registry.pem";
    private String caPemFilePath = tlsFolder + File.separator  + caPemFile;

    @Value("${hyperledger.fabric.ca.admin.name}")
    private String caAdminName;

    @Value("${hyperledger.fabric.ca.admin.password}")
    private String caAdminSecret;

    @Value("${hyperledger.fabric.user.msp}")
    private String userMspId;

    @Value("${hyperledger.fabric.user.affiliation}")
    private String userAffiliation;

    private String user1MspName = "user1.json";
    private String user1MspPath = mspFolder + File.separator + user1MspName;

    @Value("${hyperledger.fabric.organization.registry.name}")
    private String registryName;

    @Value("${hyperledger.fabric.organization.registry.pem}")
    private String registryPem;

    @Value("${hyperledger.fabric.organization.registry.url}")
    private String registryUrl;

    private String registryPemFile = "peer0-registry.pem";
    private String registryPemFilePath = tlsFolder + File.separator + registryPemFile;

    @Value("${hyperledger.fabric.organization.registry.name}")
    private String viewerName;

    @Value("${hyperledger.fabric.organization.viewer.name}")
    private String viewerPem;

    @Value("${hyperledger.fabric.organization.viewer.url}")
    private String viewerUrl;

    private String viewerPemFile = "peer0-viewer.pem";
    private String viewerPemFilePath = tlsFolder + File.separator + viewerPemFile;

    @Value("${hyperledger.fabric.organization.orderer.name}")
    private String ordererName;

    @Value("${hyperledger.fabric.organization.orderer.url}")
    private String ordererUrl;

    @Value("${hyperledger.fabric.organization.orderer.pem}")
    private String ordererPem;

    private String ordererPemFile = "orderer.pem";
    private String ordererPemFilePath = tlsFolder + File.separator + ordererPemFile;

    @Value("${hyperledger.fabric.channel.name}")
    private String channelName;

    public FabricConfig() {
        write(caPemFilePath, caPem);
        write(registryPemFilePath, registryPem);
        write(viewerPemFilePath, viewerPem);
    }

    private void write(String filePath, String contents) {
        File file = new File(filePath);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(contents.getBytes());
            fos.flush();
        } catch (IOException exception) {
            throw new ServerException(ExceptionStatus.FILE_IO_EXCEPTION);
        }
    }
}
