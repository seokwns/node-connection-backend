package node.connection.service;

import node.connection.dto.wallet.UserWalletCreateRequest;
import node.connection.utils.WalletManager;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final WalletManager walletManager;

    public WalletService(@Autowired WalletManager walletManager) {
        this.walletManager = walletManager;
    }

    public Wallet createAndOpenUserWallet(UserWalletCreateRequest request) {
        String phoneNumber = request.phoneNumber();
        String password = request.password();

        return walletManager.createUserWallet(phoneNumber, password);
    }

    public Wallet openUserWallet(String phoneNumber, String password) {
        return walletManager.openUserWallet(phoneNumber, password);
    }
}
