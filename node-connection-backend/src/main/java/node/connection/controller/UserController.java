package node.connection.controller;

import node.connection._core.response.Response;
import node.connection._core.security.CustomUserDetails;
import node.connection.dto.registry.RegistryDocumentDto;
import node.connection.dto.registry.request.FindUserRegistryDocumentsRequest;
import node.connection.service.FabricService;
import node.connection.service.UserService;
import node.connection.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private final FabricService fabricService;

    private final WalletService walletService;

    public UserController(
            @Autowired UserService userService,
            @Autowired FabricService fabricService,
            @Autowired WalletService walletService
    ) {
        this.userService = userService;
        this.fabricService = fabricService;
        this.walletService = walletService;
    }

    @PostMapping("/registry")
    public ResponseEntity<?> findRegistryDocuments(@RequestBody FindUserRegistryDocumentsRequest request) {
        List<RegistryDocumentDto> documentDtos = this.userService.findRegistryDocuments(request);
        return ResponseEntity.ok().body(Response.success(documentDtos));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@AuthenticationPrincipal CustomUserDetails userDetails) {
        this.fabricService.register(userDetails);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        this.fabricService.delete(userDetails);
        return ResponseEntity.ok().body(Response.success(null));
    }
}
