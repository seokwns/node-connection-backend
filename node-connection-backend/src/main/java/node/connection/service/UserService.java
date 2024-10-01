package node.connection.service;

import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.client.BadRequestException;
import node.connection._core.exception.server.ServerException;
import node.connection._core.security.CustomUserDetails;
import node.connection._core.utils.AccessControl;
import node.connection._core.utils.Mapper;
import node.connection.data.BuildingDescription;
import node.connection.data.RegistryDocument;
import node.connection.data.IssuerData;
import node.connection.dto.user.request.JoinDTO;
import node.connection.dto.user.response.IssuanceHistoryDto;
import node.connection.entity.IssuanceHistory;
import node.connection.entity.UserAccount;
import node.connection.entity.constant.Role;
import node.connection.hyperledger.FabricConfig;
import node.connection.hyperledger.fabric.FabricConnector;
import node.connection.hyperledger.fabric.FabricProposalResponse;
import node.connection.hyperledger.fabric.ca.CAEnrollment;
import node.connection.repository.CourtRepository;
import node.connection.repository.IssuanceHistoryRepository;
import node.connection.repository.UserAccountRepository;
import org.hyperledger.fabric.sdk.Enrollment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserService {

    private final FabricService fabricService;

    private final FabricConfig fabricConfig;

    private final Mapper objectMapper;

    private final UserAccountRepository userAccountRepository;

    private final CourtRepository courtRepository;

    private final IssuanceHistoryRepository issuanceHistoryRepository;

    private final AccessControl accessControl;

    private final PasswordEncoder passwordEncoder;


    public UserService(
            @Autowired FabricService fabricService,
            @Autowired FabricConfig fabricConfig,
            @Autowired Mapper objectMapper,
            @Autowired UserAccountRepository userAccountRepository,
            @Autowired CourtRepository courtRepository,
            @Autowired IssuanceHistoryRepository issuanceHistoryRepository,
            @Autowired AccessControl accessControl,
            @Autowired PasswordEncoder passwordEncoder
    ) {
        this.fabricService = fabricService;
        this.fabricConfig = fabricConfig;
        this.objectMapper = objectMapper;
        this.userAccountRepository = userAccountRepository;
        this.courtRepository = courtRepository;
        this.issuanceHistoryRepository = issuanceHistoryRepository;
        this.accessControl = accessControl;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(CustomUserDetails userDetails, JoinDTO joinDTO) {
        this.accessControl.hasAnonymousRole(userDetails);

        UserAccount userAccount = userDetails.getUserAccount();
        String id = userAccount.getFabricId();
        String msp = userAccount.getMspId();

        if (this.userAccountRepository.existsByFabricId(id)) {
            throw new BadRequestException(ExceptionStatus.ALREADY_REGISTERED);
        }

        if (joinDTO.courtCode() != null) {
            this.courtRepository.findByRegisterCode(joinDTO.courtCode())
                    .orElseThrow(() -> new BadRequestException(ExceptionStatus.INVALID_COURT_CODE));
        }

        Enrollment e = this.fabricService.register(userDetails);
        CAEnrollment caEnrollment = CAEnrollment.of(e);
        String enrollment = caEnrollment.serialize(objectMapper);
        userAccount.setEnrollment(enrollment);

        if (msp.equals(FabricService.VIEWER_MSP)) {
            userAccount.setRole(Role.VIEWER);
        }
        else if (msp.equals(FabricService.REGISTRY_MSP)) {
            userAccount.setRole(Role.REGISTRY);
        }

        String encodedSecret = this.passwordEncoder.encode(userAccount.getSecret());
        userAccount.setSecret(encodedSecret);
        userAccount.setUserName(joinDTO.username());
        userAccount.setPhoneNumber(joinDTO.phoneNumber());
        userAccount.setEmail(joinDTO.email());

        this.userAccountRepository.save(userAccount);
    }

    public void login(CustomUserDetails userDetails) {
        UserAccount userAccount = userDetails.getUserAccount();
        String id = userAccount.getFabricId();

        UserAccount _userAccount = this.userAccountRepository.findByFabricId(id)
                .orElseThrow(() -> new BadRequestException(ExceptionStatus.USER_NOT_FOUND));

        if (!passwordEncoder.matches(userAccount.getSecret(), _userAccount.getSecret())) {
            throw new BadRequestException(ExceptionStatus.INVALID_PASSWORD);
        }

        this.fabricService.getConnectorById(id);
    }

    @Transactional
    public String issuance(CustomUserDetails userDetails, String documentId) {
        UserAccount userAccount = userDetails.getUserAccount();
        String id = userAccount.getFabricId();

        FabricConnector connector = this.fabricService.getConnectorById(id);

        connector.setChaincode(FabricConfig.REGISTRY_CHAIN_CODE, this.fabricConfig.getRegistryChainCodeVersion());
        FabricProposalResponse response = connector.query("GetRegistryDocumentByID", List.of(documentId));
        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_QUERY_ERROR);
        }

        String payload = response.getPayload();
        RegistryDocument document = this.objectMapper.readValue(payload, RegistryDocument.class);
        List<BuildingDescription> buildingDescriptions = document.getTitleSection().getBuildingDescription();
        String locationNumber = buildingDescriptions.get(buildingDescriptions.size() - 1).getLocationNumber();


        IssuerData issuerData = new IssuerData(
                userAccount.getFabricId(),
                userAccount.getUserName(),
                userAccount.getPhoneNumber(),
                userAccount.getEmail()
        );

        List<String> params = List.of(
                this.objectMapper.writeValueAsString(issuerData),
                documentId
        );

        connector.setChaincode(FabricConfig.ISSUANCE_CHAIN_CODE, this.fabricConfig.getIssuanceChainCodeVersion());
        response = connector.invoke("issuance", params);
        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }

        String issuanceHash = response.getPayload();

        IssuanceHistory issuanceHistory = IssuanceHistory.builder()
                .userAccount(userAccount)
                .issuanceHash(issuanceHash)
                .registryDocumentId(document.getId())
                .address(locationNumber)
                .expiredAt(LocalDateTime.now().plusDays(90))
                .build();

        this.issuanceHistoryRepository.save(issuanceHistory);

        return issuanceHash;
    }

    public List<IssuanceHistoryDto> getIssuanceHistories(CustomUserDetails userDetails) {
        UserAccount userAccount = userDetails.getUserAccount();
        List<IssuanceHistory> histories = this.issuanceHistoryRepository.findAllByUserAccount(userAccount);
        return histories.stream()
                .map(history -> new IssuanceHistoryDto(
                        history.getIssuanceHash(),
                        history.getAddress(),
                        history.getCreatedAt(),
                        history.getExpiredAt()
                ))
                .toList();
    }
}
