package node.connection.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.client.BadRequestException;
import node.connection._core.exception.server.ServerException;
import node.connection._core.security.CustomUserDetails;
import node.connection._core.utils.AccessControl;
import node.connection._core.utils.Mapper;
import node.connection.dto.root.response.FabricCourtRequest;
import node.connection.dto.user.request.JoinDTO;
import node.connection.entity.UserAccount;
import node.connection.entity.constant.Role;
import node.connection.hyperledger.FabricConfig;
import node.connection.hyperledger.fabric.FabricProposalResponse;
import node.connection.hyperledger.fabric.ca.CAEnrollment;
import node.connection.repository.CourtRepository;
import node.connection.repository.UserAccountRepository;
import org.hyperledger.fabric.sdk.Enrollment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class UserService {

    private final FabricService fabricService;

    private final FabricConfig fabricConfig;

    private final Mapper objectMapper;

    private final UserAccountRepository userAccountRepository;

    private final CourtRepository courtRepository;

    private final AccessControl accessControl;

    private final PasswordEncoder passwordEncoder;


    public UserService(
            @Autowired FabricService fabricService,
            @Autowired FabricConfig fabricConfig,
            @Autowired Mapper objectMapper,
            @Autowired UserAccountRepository userAccountRepository,
            @Autowired CourtRepository courtRepository,
            @Autowired AccessControl accessControl,
            @Autowired PasswordEncoder passwordEncoder
    ) {
        this.fabricService = fabricService;
        this.fabricConfig = fabricConfig;
        this.objectMapper = objectMapper;
        this.userAccountRepository = userAccountRepository;
        this.courtRepository = courtRepository;
        this.accessControl = accessControl;
        this.passwordEncoder = passwordEncoder;
    }

    public List<FabricCourtRequest> findRequestsByUser(CustomUserDetails userDetails) {
        this.fabricService.setChaincode("court", this.fabricConfig.getCourtChainCodeVersion());

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
}
