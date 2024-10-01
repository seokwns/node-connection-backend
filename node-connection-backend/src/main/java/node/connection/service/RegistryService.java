package node.connection.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection._core.security.CustomUserDetails;
import node.connection._core.utils.Mapper;
import node.connection.data.IssuanceData;
import node.connection.data.RegistryDocument;
import node.connection.dto.registry.RegistryDocumentDto;
import node.connection.dto.registry.response.RegistryDocumentByHashDto;
import node.connection.entity.UserAccount;
import node.connection.hyperledger.FabricConfig;
import node.connection.hyperledger.fabric.FabricConnector;
import node.connection.hyperledger.fabric.FabricProposalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RegistryService {

    private final FabricService fabricService;

    private final FabricConfig fabricConfig;

    private final Mapper objectMapper;


    public RegistryService(
            @Autowired FabricService fabricService,
            @Autowired FabricConfig fabricConfig,
            @Autowired Mapper objectMapper
    ) {
        this.fabricService = fabricService;
        this.fabricConfig = fabricConfig;
        this.objectMapper = objectMapper;
    }

    public RegistryDocumentDto getRegistryDocumentById(CustomUserDetails userDetails, String id) {
        FabricConnector connector = this.fabricService.getConnectorById(userDetails.getUsername());

        List<String> params = List.of(id);
        connector.setChaincode(FabricConfig.REGISTRY_CHAIN_CODE, this.fabricConfig.getRegistryChainCodeVersion());
        FabricProposalResponse response = connector.query("GetRegistryDocumentByID", params);

        String payload = response.getPayload();
        return this.objectMapper.readValue(payload, RegistryDocumentDto.class);
    }

    public List<RegistryDocumentDto> getRegistryDocumentByAddress(CustomUserDetails userDetails, String address, String detailAddress) {
        FabricConnector connector = this.fabricService.getConnectorById(userDetails.getUsername());
        connector.setChaincode(FabricConfig.REGISTRY_CHAIN_CODE, this.fabricConfig.getRegistryChainCodeVersion());

        List<String> params = List.of(address, detailAddress == null ? "" : detailAddress);
        FabricProposalResponse response = connector.query("GetRegistryDocumentByAddress", params);

        String payload = response.getPayload();
        return this.objectMapper.readValue(payload, new TypeReference<List<RegistryDocumentDto>>() {});
    }

    public RegistryDocumentByHashDto getRegistryDocumentByHash(CustomUserDetails userDetails, String hash) {
        UserAccount userAccount = userDetails.getUserAccount();
        String id = userAccount.getFabricId();
        FabricConnector connector = this.fabricService.getConnectorById(id);
        connector.setChaincode(FabricConfig.ISSUANCE_CHAIN_CODE, this.fabricConfig.getIssuanceChainCodeVersion());

        FabricProposalResponse response = connector.query("GetIssuanceDataByHash", List.of(hash));
        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_QUERY_ERROR);
        }

        String payload = response.getPayload();
        IssuanceData issuanceData = this.objectMapper.readValue(payload, IssuanceData.class);

        String documentId = issuanceData.registryDocument().getId();
        connector.setChaincode(FabricConfig.REGISTRY_CHAIN_CODE, this.fabricConfig.getRegistryChainCodeVersion());

        response = connector.query("GetRegistryDocumentByID", List.of(documentId));
        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_QUERY_ERROR);
        }

        payload = response.getPayload();
        RegistryDocument latestDocument = this.objectMapper.readValue(payload, RegistryDocument.class);

        return new RegistryDocumentByHashDto(
                issuanceData.txId(),
                issuanceData.issuerName(),
                issuanceData.issuanceDate(),
                issuanceData.expirationDate(),
                issuanceData.registryDocument(),
                latestDocument
        );
    }
}
