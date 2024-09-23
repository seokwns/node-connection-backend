package node.connection.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import node.connection._core.security.CustomUserDetails;
import node.connection._core.utils.Mapper;
import node.connection.dto.registry.RegistryDocumentDto;
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

    public List<RegistryDocumentDto> getRegistryDocumentByAddress(CustomUserDetails userDetails, String address) {
        log.debug("address: {}", address);
        FabricConnector connector = this.fabricService.getConnectorById(userDetails.getUsername());

        List<String> params = List.of(address);
        connector.setChaincode(FabricConfig.REGISTRY_CHAIN_CODE, this.fabricConfig.getRegistryChainCodeVersion());
        FabricProposalResponse response = connector.query("GetRegistryDocumentByLocationNumber", params);

        String payload = response.getPayload();
        return this.objectMapper.readValue(payload, new TypeReference<List<RegistryDocumentDto>>() {});
    }
}
