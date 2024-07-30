package node.connection.utils;

import lombok.RequiredArgsConstructor;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Component
public class WalletManager {
    static Logger log = LoggerFactory.getLogger(WalletManager.class);

    private final Pool pool;

    @Value("${hyperledger.indy.wallet.user-wallet-storage}")
    private String userWalletStorage;

    @Value(("${hyperledger.indy.wallet.court-wallet-storage}"))
    private String courtWalletStorage;

    public WalletManager() throws Exception {
        this.pool = createIndyConnectionPool();
    }

    private Pool createIndyConnectionPool() throws IndyException, ExecutionException, InterruptedException {
        String poolName = "NodeConnectionPool";
        String poolConfig = "{\"genesis_txn\": \"src/main/java/node/connection/utils/indy/pool.txn\"} ";

        Pool.setProtocolVersion(2);
        Pool pool = null;

        try {
            pool = Pool.openPoolLedger(poolName, "{}").get();
        } catch (Exception exception) {
            Pool.createPoolLedgerConfig(poolName, poolConfig);
            pool = Pool.openPoolLedger(poolName, "{}").get();
        }

        log.info("Hyperledger Indy Pool created and opened successfully.");
        return pool;
    }

    public Wallet createUserWallet(String phoneNumber, String password) {
        log.info("일반 유저 지갑 생성 시작 | " + phoneNumber);
        String userConfig = createUserWalletConfig(phoneNumber);
        String credential = createWalletCredential(password);

        Wallet wallet;
        try {
            Wallet.createWallet(userConfig, credential).get();
            wallet = Wallet.openWallet(userConfig, credential).get();
        } catch (InterruptedException | ExecutionException | IndyException exception) {
            throw new ServerException(ExceptionStatus.WALLET_CREATION_ERROR);
        }

        log.info("일반 유저 지갑 생성 완료 | " + phoneNumber);
        return wallet;
    }

    public Wallet createCourtWallet(String court, String department, String location, String password) {
        log.info("등기소 지갑 생성 시작 | " + court + " | " + department + " | " + location);
        String courtConfig = createCourtWalletConfig(court, department, location);
        String credential = createWalletCredential(password);

        Wallet wallet;
        try {
            Wallet.createWallet(courtConfig, credential).get();
            wallet = Wallet.openWallet(courtConfig, credential).get();
        } catch (InterruptedException | ExecutionException | IndyException e) {
            throw new ServerException(ExceptionStatus.WALLET_CREATION_ERROR);
        }

        log.info("등기소 지갑 생성 완료 | " + court + " | " + department + " | " + location);
        return wallet;
    }

    public Wallet openUserWallet(String phoneNumber, String password) {
        String userConfig = createUserWalletConfig(phoneNumber);
        String credential = createWalletCredential(password);

        try {
            return Wallet.openWallet(userConfig, credential).get();
        } catch (InterruptedException | ExecutionException | IndyException e) {
            throw new ServerException(ExceptionStatus.WALLET_OPEN_ERROR);
        }
    }

    private String createUserWalletConfig(String phoneNumber) {
        JSONObject storageConfig = new JSONObject().put("path", this.userWalletStorage);

        return new JSONObject()
                .put("id", phoneNumber)
                .put("storage_config", storageConfig)
                .toString();
    }

    private String createCourtWalletConfig(String court, String department, String location) {
        JSONObject storageConfig = new JSONObject().put("path", this.courtWalletStorage);

        return new JSONObject()
                .put("id", this.getCourtId(court, department, location))
                .put("storage_config", storageConfig)
                .toString();
    }

    private String createWalletCredential(String password) {
        return new JSONObject()
                .put("key", password)
                .toString();
    }

    private String getCourtId(String court, String department, String location) {
        return court + "_" + department + "_" + location;
    }
}
