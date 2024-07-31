package node.connection.utils;

import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection.wallet.WalletConfigBuilder;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class WalletManager {
    static Logger log = LoggerFactory.getLogger(WalletManager.class);

    private final Pool pool;

    private final WalletConfigBuilder walletConfigBuilder;

    public WalletManager(
            @Autowired WalletConfigBuilder walletConfigBuilder
    ) throws Exception {
        this.pool = createIndyConnectionPool();
        this.walletConfigBuilder = walletConfigBuilder;
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
        String userConfig = this.walletConfigBuilder.getUserWalletConfig(phoneNumber);
        Wallet wallet = createWallet(userConfig, password);

        log.info("일반 유저 지갑 생성 완료 | " + phoneNumber);
        return wallet;
    }

    public Wallet createCourtWallet(String court, String department, String location, String password) {
        String courtConfig = this.walletConfigBuilder.getCourtWalletConfig(court, department, location);
        Wallet wallet = createWallet(courtConfig, password);

        log.info("등기소 지갑 생성 완료 | " + court + " | " + department + " | " + location);
        return wallet;
    }

    private Wallet createWallet(String config, String password) {
        String credential = this.walletConfigBuilder.getWalletCredential(password);

        try {
            Wallet.createWallet(config, credential).get();
            return Wallet.openWallet(config, credential).get();
        } catch (InterruptedException | ExecutionException | IndyException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.WALLET_CREATION_ERROR);
        }
    }

    public Wallet openUserWallet(String phoneNumber, String password) {
        String config = this.walletConfigBuilder.getUserWalletConfig(phoneNumber);
        return this.openWallet(config, password);
    }

    public Wallet openCourtWallet(String court, String department, String location, String password) {
        String config = this.walletConfigBuilder.getCourtWalletConfig(court, department, location);
        return this.openWallet(config, password);
    }

    private Wallet openWallet(String config, String password) {
        String credential = this.walletConfigBuilder.getWalletCredential(password);

        try {
            Wallet wallet = Wallet.openWallet(config, credential).get();
            log.info("지갑 오픈 완료 | " + config);
            return wallet;
        } catch (InterruptedException | ExecutionException | IndyException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.WALLET_OPEN_ERROR);
        }
    }
}
