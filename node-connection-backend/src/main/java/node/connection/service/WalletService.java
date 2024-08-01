package node.connection.service;

import node.connection.dto.wallet.CourtWalletCreateRequest;
import node.connection.dto.wallet.UserWalletCreateRequest;
import node.connection.hyperledger.WalletManager;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final WalletManager walletManager;

    public WalletService(@Autowired WalletManager walletManager) {
        this.walletManager = walletManager;
    }

    public void createUserWallet(UserWalletCreateRequest request) {
        String phoneNumber = request.phoneNumber();
        String password = request.password();
        walletManager.createUserWallet(phoneNumber, password);
    }

    public Wallet openUserWallet(String phoneNumber, String password) {
        return walletManager.openUserWallet(phoneNumber, password);
    }

    public void createCourtWallet(CourtWalletCreateRequest request) {
        String court = request.court();
        String department = request.department();
        String location = request.location();
        String password = request.password();
        walletManager.createCourtWallet(court, department, location, password);
    }

    public Wallet openCourtWallet(String court, String department, String location, String password) {
        return walletManager.openCourtWallet(court, department, location, password);
    }
}
