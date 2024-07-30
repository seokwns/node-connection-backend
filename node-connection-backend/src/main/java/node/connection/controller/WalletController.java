package node.connection.controller;

import node.connection._core.response.Response;
import node.connection.dto.wallet.UserWalletCreateRequest;
import node.connection.service.WalletService;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
public class WalletController {
    private final WalletService walletService;

    public WalletController(@Autowired WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/user")
    public ResponseEntity<?> createUserWallet(@RequestBody UserWalletCreateRequest walletCreateRequest) {
        Wallet wallet = walletService.createUserWallet(walletCreateRequest);
        return ResponseEntity.ok().body(Response.success(wallet));
    }
}
