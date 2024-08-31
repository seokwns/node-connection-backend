package node.connection.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection._core.security.CustomUserDetails;
import node.connection.dto.court.request.AddCourtMemberRequest;
import node.connection.dto.court.request.CourtCreateRequest;
import node.connection.dto.court.response.FabricCourt;
import node.connection.dto.wallet.CourtWalletCreateRequest;
import node.connection.entity.Court;
import node.connection.entity.CourtWalletConfig;
import node.connection.entity.Jurisdiction;
import node.connection.hyperledger.fabric.FabricConnector;
import node.connection.hyperledger.fabric.FabricProposalResponse;
import node.connection.repository.CourtRepository;
import node.connection.repository.CourtWalletConfigRepository;
import node.connection.repository.JurisdictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CourtService {

    private final FabricService fabricService;

    private final WalletService walletService;

    private final CourtRepository courtRepository;

    private final CourtWalletConfigRepository walletConfigRepository;

    private final JurisdictionRepository jurisdictionRepository;

    private final ObjectMapper objectMapper;


    public CourtService(
            @Autowired FabricService fabricService,
            @Autowired WalletService walletService,
            @Autowired CourtRepository courtRepository,
            @Autowired CourtWalletConfigRepository walletConfigRepository,
            @Autowired JurisdictionRepository jurisdictionRepository,
            @Autowired ObjectMapper objectMapper
    ) {
        this.fabricService = fabricService;
        this.walletService = walletService;
        this.courtRepository = courtRepository;
        this.walletConfigRepository = walletConfigRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void createCourt(CustomUserDetails userDetails, CourtCreateRequest request) {
        String courtId = request.court() + "_" + request.support() + "_" + request.office();

        Court court = Court.of(request);
        this.courtRepository.save(court);

        CourtWalletConfig walletConfig = CourtWalletConfig.of(courtId, request.walletPassword(), court);
        this.walletConfigRepository.save(walletConfig);

        List<Jurisdiction> jurisdictions = new ArrayList<>();
        request.jurisdictions().forEach(jurisdiction -> {
            jurisdictions.add(Jurisdiction.of(jurisdiction, court));
        });
        this.jurisdictionRepository.saveAll(jurisdictions);

        this.walletService.createCourtWallet(new CourtWalletCreateRequest(request.court(), request.support(), request.office(), request.walletPassword()));

        FabricConnector connector = this.fabricService.getConnectorById(userDetails.getUsername());
        connector.setChaincode("court", "1.0.0");

        List<String> params = List.of(
                courtId,
                request.court(),
                request.support(),
                request.office(),
                userDetails.getUsername()
        );
        FabricProposalResponse response = connector.invoke("RegistryCourt", params);

        if (!response.getSuccess()) {
            log.error("court creation error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    public FabricCourt getCourtById(String id) {
        this.fabricService.setChaincode("court", "1.0.0");
        FabricProposalResponse response = this.fabricService.query("GetCourtByID", List.of(id));

        if (!response.getSuccess()) {
            log.error("court creation error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }

        try {
            return this.objectMapper.readValue(response.getPayload(), FabricCourt.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.JSON_PROCESSING_EXCEPTION);
        }
    }

    public void addCourtMember(CustomUserDetails userDetails, AddCourtMemberRequest request) {
        String courtId = request.court() + "_" + request.support() + "_" + request.office();

        FabricConnector connector = this.fabricService.getConnectorById(userDetails.getUsername());
        connector.setChaincode("court", "1.0.1");

        List<String> params = List.of(
                courtId,
                request.memberId()
        );
        FabricProposalResponse response = connector.invoke("AddMember", params);

        if (!response.getSuccess()) {
            log.error("add court member error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }
}
