package node.connection.service;

import node.connection.dto.wallet.CourtWalletCreateRequest;
import node.connection.dto.wallet.UserWalletCreateRequest;
import node.connection.hyperledger.indy.IndyConnector;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final IndyConnector indyConnector;

    public WalletService(@Autowired IndyConnector indyConnector) {
        this.indyConnector = indyConnector;
    }

    public void createUserWallet(UserWalletCreateRequest request) {
        String phoneNumber = request.phoneNumber();
        String password = request.password();
        this.indyConnector.createUserWallet(phoneNumber, password);
    }

    public Wallet openUserWallet(String phoneNumber, String password) {
        return this.indyConnector.openUserWallet(phoneNumber, password);
    }

    public void createCourtWallet(CourtWalletCreateRequest request) {
        String court = request.court();
        String support = request.support();
        String office = request.office();
        String password = request.password();
        this.indyConnector.createCourtWallet(court, support, office, password);
    }

    public Wallet openCourtWallet(String court, String department, String location, String password) {
        return this.indyConnector.openCourtWallet(court, department, location, password);
    }

    public void closeWallet(Wallet wallet) {
        this.indyConnector.closeWallet(wallet);
    }
}
