package node.connection.controller;

import node.connection._core.response.Response;
import node.connection.dto.wallet.UserWalletCreateRequest;
import node.connection.service.WalletService;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
public class WalletController {
    private final WalletService walletService;

    public WalletController(@Autowired WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/user")
    public ResponseEntity<?> createAndOpenUserWallet(@RequestBody UserWalletCreateRequest walletCreateRequest) {
        Wallet wallet = walletService.createAndOpenUserWallet(walletCreateRequest);
        return ResponseEntity.ok().body(Response.success(wallet));
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserWallet(
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("password") String password
    ) {
        Wallet wallet = walletService.openUserWallet(phoneNumber, password);
        return ResponseEntity.ok().body(Response.success(wallet));
    }
}
