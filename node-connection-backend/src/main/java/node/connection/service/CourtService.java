package node.connection.service;

import lombok.extern.slf4j.Slf4j;
import node.connection._core.security.CustomUserDetails;
import node.connection._core.utils.AccessControl;
import node.connection._core.utils.Mapper;
import node.connection.data.registry.RegistryBuilder;
import node.connection.dto.root.request.CourtCreateRequest;
import node.connection.entity.Court;
import node.connection.entity.Jurisdiction;
import node.connection.hyperledger.FabricConfig;
import node.connection.hyperledger.fabric.FabricConnector;
import node.connection.hyperledger.fabric.NetworkConfig;
import node.connection.repository.CourtRepository;
import node.connection.repository.JurisdictionRepository;
import node.connection.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CourtService {

    private final FabricService fabricService;

    private final FabricConfig fabricConfig;

    private final CourtRepository courtRepository;

    private final JurisdictionRepository jurisdictionRepository;

    private final UserAccountRepository userAccountRepository;

    private final Mapper objectMapper;

    private final RegistryBuilder registryBuilder;

    private final AccessControl accessControl;


    public CourtService(
            @Autowired FabricService fabricService,
            @Autowired FabricConfig fabricConfig,
            @Autowired CourtRepository courtRepository,
            @Autowired JurisdictionRepository jurisdictionRepository,
            @Autowired UserAccountRepository userAccountRepository,
            @Autowired Mapper objectMapper,
            @Autowired RegistryBuilder registryBuilder,
            @Autowired AccessControl accessControl
    ) {
        this.fabricService = fabricService;
        this.fabricConfig = fabricConfig;
        this.courtRepository = courtRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.userAccountRepository = userAccountRepository;
        this.objectMapper = objectMapper;
        this.registryBuilder = registryBuilder;
        this.accessControl = accessControl;
    }

    @Transactional
    public void createCourt(CustomUserDetails userDetails, CourtCreateRequest request) {
        this.accessControl.hasRootRole(userDetails);

        Court court = Court.of(request);
        this.courtRepository.save(court);

        List<Jurisdiction> jurisdictions = new ArrayList<>();
        request.getJurisdictions().forEach(jurisdiction -> jurisdictions.add(Jurisdiction.of(jurisdiction, court)));
        this.jurisdictionRepository.saveAll(jurisdictions);

        FabricConnector connector = this.fabricService.getRootFabricConnector();
        NetworkConfig networkConfig = this.fabricService.getNetworkConfig();
        networkConfig.setChannelName(request.getChannelName());
        connector.connectToChannel(networkConfig);
    }
}
