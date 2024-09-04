package node.connection.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection._core.security.CustomUserDetails;
import node.connection._core.utils.Mapper;
import node.connection.data.court.CourtRequest;
import node.connection.data.registry.RegistryBuilder;
import node.connection.data.registry.RegistryDocument;
import node.connection.dto.court.request.AddCourtMemberRequest;
import node.connection.dto.court.request.CourtCreateRequest;
import node.connection.dto.court.request.DeleteCourtMemberRequest;
import node.connection.dto.court.request.FinalizeCourtRequest;
import node.connection.dto.court.response.FabricCourt;
import node.connection.dto.court.response.FabricCourtRequest;
import node.connection.dto.registry.request.RegistryCreateRequest;
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
import java.util.UUID;

@Service
@Slf4j
public class CourtService {

    private final FabricService fabricService;

    private final WalletService walletService;

    private final CourtRepository courtRepository;

    private final CourtWalletConfigRepository walletConfigRepository;

    private final JurisdictionRepository jurisdictionRepository;

    private final Mapper objectMapper;

    private final RegistryBuilder registryBuilder;

    private String COURT_CHAINCODE_VERSION = "1.0.2";


    public CourtService(
            @Autowired FabricService fabricService,
            @Autowired WalletService walletService,
            @Autowired CourtRepository courtRepository,
            @Autowired CourtWalletConfigRepository walletConfigRepository,
            @Autowired JurisdictionRepository jurisdictionRepository,
            @Autowired Mapper objectMapper,
            @Autowired RegistryBuilder registryBuilder
    ) {
        this.fabricService = fabricService;
        this.walletService = walletService;
        this.courtRepository = courtRepository;
        this.walletConfigRepository = walletConfigRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.objectMapper = objectMapper;
        this.registryBuilder = registryBuilder;
    }

    @Transactional
    public void createCourt(CustomUserDetails userDetails, CourtCreateRequest request) {
        String courtId = request.getCourtId();

        Court court = Court.of(request);
        this.courtRepository.save(court);

        CourtWalletConfig walletConfig = CourtWalletConfig.of(courtId, request.getWalletPassword(), court);
        this.walletConfigRepository.save(walletConfig);

        List<Jurisdiction> jurisdictions = new ArrayList<>();
        request.getJurisdictions().forEach(jurisdiction -> {
            jurisdictions.add(Jurisdiction.of(jurisdiction, court));
        });
        this.jurisdictionRepository.saveAll(jurisdictions);

        this.walletService.createCourtWallet(
                new CourtWalletCreateRequest(
                        request.getCourt(),
                        request.getSupport(),
                        request.getOffice(),
                        request.getWalletPassword()
                )
        );

        FabricConnector connector = this.fabricService.getConnectorById(userDetails.getUsername());
        connector.setChaincode("court", COURT_CHAINCODE_VERSION);

        List<String> params = List.of(
                courtId,
                request.getCourt(),
                request.getSupport(),
                request.getOffice(),
                userDetails.getUsername()
        );
        FabricProposalResponse response = connector.invoke("CreateCourt", params);

        if (!response.getSuccess()) {
            log.error("court creation error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    public FabricCourt getCourtById(String id) {
        this.fabricService.setChaincode("court", COURT_CHAINCODE_VERSION);
        FabricProposalResponse response = this.fabricService.query("GetCourtByID", List.of(id));

        if (!response.getSuccess()) {
            log.error("court creation error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }

        return this.objectMapper.readValue(response.getPayload(), FabricCourt.class);
    }

    public void addCourtMember(CustomUserDetails userDetails, AddCourtMemberRequest request) {
        FabricConnector connector = this.fabricService.getConnectorById(userDetails.getUsername());
        connector.setChaincode("court", COURT_CHAINCODE_VERSION);

        List<String> params = List.of(
                request.getCourtId(),
                request.getMemberId()
        );
        FabricProposalResponse response = connector.invoke("AddMember", params);

        if (!response.getSuccess()) {
            log.error("add court member error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    public void deleteCourtMember(CustomUserDetails userDetails, DeleteCourtMemberRequest request) {
        String courtId = request.getCourtId();
        FabricConnector connector = this.fabricService.getConnectorById(userDetails.getUsername());
        connector.setChaincode("court", COURT_CHAINCODE_VERSION);

        List<String> params = List.of(
                courtId,
                request.getMemberId()
        );
        FabricProposalResponse response = connector.invoke("RemoveMember", params);

        if (!response.getSuccess()) {
            log.error("add court member error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    public List<FabricCourtRequest> getUnfinalizedRequests(String id) {
        this.fabricService.setChaincode("court", COURT_CHAINCODE_VERSION);

        List<String> params = List.of(id);
        FabricProposalResponse response = this.fabricService.query("GetAllUnfinalizedRequests", params);

        if (!response.getSuccess()) {
            log.error("get unfinalized court request error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }

        if (response.getPayload().isEmpty()) {
            return List.of();
        }

        return this.objectMapper.readValue(response.getPayload(), new TypeReference<List<FabricCourtRequest>>() {});
    }

    public List<FabricCourtRequest> getFinalizedRequests(String id) {
        this.fabricService.setChaincode("court", COURT_CHAINCODE_VERSION);

        List<String> params = List.of(id);
        FabricProposalResponse response = this.fabricService.query("GetAllFinalizedRequests", params);

        if (!response.getSuccess()) {
            log.error("get finalized court request error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }

        if (response.getPayload().isEmpty()) {
            return List.of();
        }

        return this.objectMapper.readValue(response.getPayload(), new TypeReference<List<FabricCourtRequest>>() {});
    }

    public List<FabricCourtRequest> getCourtRequestsByRequestorId(CustomUserDetails userDetails) {
        this.fabricService.setChaincode("court", COURT_CHAINCODE_VERSION);

        List<String> params = List.of(userDetails.getUsername());
        FabricProposalResponse response = this.fabricService.query("GetRequestsByRequestorId", params);

        if (!response.getSuccess()) {
            log.error("get court request error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }

        if (response.getPayload().isEmpty()) {
            return List.of();
        }

        return this.objectMapper.readValue(response.getPayload(), new TypeReference<List<FabricCourtRequest>>() {});
    }

    public void createRegistryCourtRequest(CustomUserDetails userDetails, String courtId, RegistryCreateRequest request) {
        String requestId = String.valueOf(UUID.randomUUID());
        String documentId = String.valueOf(UUID.randomUUID());
        RegistryDocument registryDocument = this.registryBuilder.build(documentId, request.document());
        String documentJson = this.objectMapper.writeValueAsString(registryDocument);

        CourtRequest courtRequest = CourtRequest.builder()
                .id(requestId)
                .documentId(documentId)
                .action("CreateRegistryDocument")
                .payload(documentJson)
                .build();

        this.invokeAddRequest(userDetails.getUsername(), courtId, courtRequest);
    }

    private void invokeAddRequest(String user, String courtId, CourtRequest request) {
        FabricConnector connector = this.fabricService.getConnectorById(user);
        connector.setChaincode("court", COURT_CHAINCODE_VERSION);

        String requestJson = this.objectMapper.writeValueAsString(request);
        List<String> params = List.of(courtId, requestJson);
        FabricProposalResponse response = connector.invoke("AddRequest", params);

        if (!response.getSuccess()) {
            log.error("add court request error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    public void finalizeCourtRequest(CustomUserDetails userDetails, String courtId, FinalizeCourtRequest request) {
        FabricConnector connector = this.fabricService.getConnectorById(userDetails.getUsername());
        connector.setChaincode("court", COURT_CHAINCODE_VERSION);

        List<String> params = List.of(
                courtId,
                request.requestId(),
                request.status(),
                request.errorMessage()
        );
        FabricProposalResponse response = connector.invoke("FinalizeRequest", params);

        if (!response.getSuccess()) {
            log.error("add court request error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }
}
