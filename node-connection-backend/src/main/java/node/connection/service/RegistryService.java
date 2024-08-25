package node.connection.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection.data.registry.RegistryBuilder;
import node.connection.data.registry.RegistryDocument;
import node.connection.data.wallet.metadata.DidMetadata;
import node.connection.data.wallet.metadata.constant.MetadataType;
import node.connection.dto.registry.RegistryDocumentDto;
import node.connection.dto.registry.request.RegistryCreateRequest;
import node.connection.entity.DidEntry;
import node.connection.entity.pk.DidEntryKey;
import node.connection.hyperledger.fabric.FabricProposalResponse;
import node.connection.repository.DidEntryRepository;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class RegistryService {

    private final WalletService walletService;

    private final FabricService fabricService;

    private final ObjectMapper objectMapper;

    private final RegistryBuilder registryBuilder;

    private final DidEntryRepository didEntryRepository;

    public RegistryService(
            @Autowired WalletService walletService,
            @Autowired FabricService fabricService,
            @Autowired ObjectMapper objectMapper,
            @Autowired RegistryBuilder registryBuilder,
            @Autowired DidEntryRepository didEntryRepository
    ) {
        this.walletService = walletService;
        this.fabricService = fabricService;
        this.objectMapper = objectMapper;
        this.registryBuilder = registryBuilder;
        this.didEntryRepository = didEntryRepository;
    }

    public void createRegistryDocument(RegistryCreateRequest request) {
        Wallet wallet = null;
        try {
            String documentId = UUID.randomUUID().toString();
            wallet = this.walletService.openUserWallet(request.phoneNumber(), request.password());

            DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(wallet, "{}").get();
            String did = didResult.getDid();
            String verKey = didResult.getVerkey();
            log.info("created did: " + did + ", key: " + verKey);

            DidMetadata metadata = DidMetadata.builder()
                    .type(MetadataType.OWNER)
                    .documentId(documentId)
                    .createdAt(LocalDateTime.now())
                    .expiredAt(LocalDateTime.of(2025, 8, 22, 15, 0, 0))
                    .build();
            String metadataJson = objectMapper.writeValueAsString(metadata);

            log.info("metadata json: " + metadataJson);

            Did.setDidMetadata(wallet, did, metadataJson);

            RegistryDocument document = registryBuilder.build(documentId, request.document());
            String documentToJson = objectMapper.writeValueAsString(document);
            List<String> params = List.of(documentToJson);

            this.fabricService.setChaincode("registry", "1.0.0");
            FabricProposalResponse response = this.fabricService.invoke("CreateRegistryDocument", params);
            log.debug(String.valueOf(response));

            DidEntryKey didEntryKey = DidEntryKey.builder()
                    .phoneNumber(request.phoneNumber())
                    .did(did)
                    .build();
            DidEntry didEntry = DidEntry.builder().key(didEntryKey).verKey(verKey).build();
            didEntryRepository.save(didEntry);
            log.info("Registry document created successfully. | " + documentId);
        } catch (JsonProcessingException e) {
            throw new ServerException(ExceptionStatus.JSON_PROCESSING_EXCEPTION);
        } catch (IndyException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (wallet != null) {
                try {
                    wallet.close();
                } catch (InterruptedException | ExecutionException | IndyException e) {
                    e.printStackTrace();
                    throw new ServerException(ExceptionStatus.WALLET_CLOSE_ERROR);
                }
            }
        }
    }

    public RegistryDocumentDto getRegistryDocumentById(String id) {
        return this.fabricService.getRegistryDocumentById(id);
    }
}
