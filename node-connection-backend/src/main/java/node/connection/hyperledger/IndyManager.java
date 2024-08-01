package node.connection.hyperledger;

import lombok.extern.slf4j.Slf4j;
import node.connection.wallet.WalletConfigFactory;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IndyManager {

    private final Pool pool;
    private final WalletConfigFactory walletConfigFactory;
    private final IndyUtils indyUtils;

    public IndyManager(
            @Autowired WalletConfigFactory walletConfigFactory,
            @Autowired IndyUtils indyUtils
    ) throws Exception {
        this.walletConfigFactory = walletConfigFactory;
        this.indyUtils = indyUtils;
        this.pool = this.indyUtils.initializeIndyConnectionPool();
    }

    public void createUserWallet(String phoneNumber, String password) {
        String userConfig = this.walletConfigFactory.getUserWalletConfig(phoneNumber);
        this.indyUtils.createWallet(userConfig, password);
        log.info("일반 유저 지갑 생성 완료 | " + phoneNumber);
    }

    public void createCourtWallet(String court, String department, String location, String password) {
        String courtConfig = this.walletConfigFactory.getCourtWalletConfig(court, department, location);
        this.indyUtils.createWallet(courtConfig, password);
        log.info("등기소 지갑 생성 완료 | " + court + " | " + department + " | " + location);
    }

    public Wallet openUserWallet(String phoneNumber, String password) {
        String config = this.walletConfigFactory.getUserWalletConfig(phoneNumber);
        return this.indyUtils.openWallet(config, password);
    }

    public Wallet openCourtWallet(String court, String department, String location, String password) {
        String config = this.walletConfigFactory.getCourtWalletConfig(court, department, location);
        return this.indyUtils.openWallet(config, password);
    }
}
