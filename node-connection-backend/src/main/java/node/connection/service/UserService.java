package node.connection.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection.data.wallet.metadata.DidMetadata;
import node.connection.dto.registry.RegistryDocumentDto;
import node.connection.dto.registry.request.FindUserRegistryDocumentsRequest;
import node.connection.repository.DidEntryRepository;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class UserService {

    private final FabricService fabricService;

    private final WalletService walletService;

    private final DidEntryRepository didEntryRepository;

    private final ObjectMapper objectMapper;

    public UserService(
            @Autowired FabricService fabricService,
            @Autowired WalletService walletService,
            @Autowired DidEntryRepository didEntryRepository,
            @Autowired ObjectMapper objectMapper
    ) {
        this.fabricService = fabricService;
        this.walletService = walletService;
        this.didEntryRepository = didEntryRepository;
        this.objectMapper = objectMapper;
    }

    public List<RegistryDocumentDto> findRegistryDocuments(FindUserRegistryDocumentsRequest request) throws RuntimeException {
        Wallet wallet = this.walletService.openUserWallet(request.phoneNumber(), request.password());
        List<RegistryDocumentDto> registryDocumentDtos = new ArrayList<>();

        try {
            String didListJson = Did.getListMyDidsWithMeta(wallet).get();
            log.info("did list json: " + didListJson);

            JSONArray didList = new JSONArray(didListJson);

            for (int i = 0; i < didList.length(); i++) {
                JSONObject object = didList.getJSONObject(i);

                String did = object.getString("did");
                String metadataStr = object.getString("metadata");
                DidMetadata metadata = this.objectMapper.readValue(metadataStr, DidMetadata.class);
                RegistryDocumentDto documentDto = this.fabricService.getRegistryDocumentById(metadata.getDocumentId());
                registryDocumentDtos.add(documentDto);
            }

            return registryDocumentDtos;
        } catch (IndyException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.WALLET_CLOSE_ERROR);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.JSON_PROCESSING_EXCEPTION);
        }
    }
}
