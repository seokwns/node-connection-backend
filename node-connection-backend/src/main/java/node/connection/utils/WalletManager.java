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
        String userConfig = createUserWalletConfig(phoneNumber);
        Wallet wallet = createWallet(userConfig, password);

        log.info("일반 유저 지갑 생성 완료 | " + phoneNumber);
        return wallet;
    }

    public Wallet createCourtWallet(String court, String department, String location, String password) {
        String courtConfig = createCourtWalletConfig(court, department, location);
        Wallet wallet = createWallet(courtConfig, password);

        log.info("등기소 지갑 생성 완료 | " + court + " | " + department + " | " + location);
        return wallet;
    }

    private Wallet createWallet(String config, String password) {
        String credential = createWalletCredential(password);

        try {
            Wallet.createWallet(config, credential).get();
            return Wallet.openWallet(config, credential).get();
        } catch (InterruptedException | ExecutionException | IndyException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.WALLET_CREATION_ERROR);
        }
    }

    public Wallet openUserWallet(String phoneNumber, String password) {
        String config = createUserWalletConfig(phoneNumber);
        return this.openWallet(config, password);
    }

    public Wallet openCourtWallet(String court, String department, String location, String password) {
        String config = createCourtWalletConfig(court, department, location);
        return this.openWallet(config, password);
    }

    private Wallet openWallet(String config, String password) {
        String credential = createWalletCredential(password);

        try {
            Wallet wallet = Wallet.openWallet(config, credential).get();
            log.info("지갑 오픈 완료 | " + config);
        } catch (InterruptedException | ExecutionException | IndyException e) {
            e.printStackTrace();
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
