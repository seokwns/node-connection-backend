package node.connection.controller;

import node.connection._core.response.Response;
import node.connection._core.security.CustomUserDetails;
import node.connection.dto.root.request.CourtCreateRequest;
import node.connection.dto.root.request.FabricPeerAddRequest;
import node.connection.service.CourtService;
import node.connection.service.FabricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/root")
public class RootController {

    private final CourtService courtService;

    private final FabricService fabricService;


    public RootController(@Autowired CourtService courtService,
                          @Autowired FabricService fabricService
    ) {
        this.courtService = courtService;
        this.fabricService = fabricService;
    }

    @PostMapping("/court")
    public ResponseEntity<?> createNewCourt(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CourtCreateRequest request
    ) {
        this.courtService.createCourt(userDetails, request);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PostMapping("/hyperledger/fabric/registry/peer")
    public ResponseEntity<?> addPeer(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestBody FabricPeerAddRequest request
    ) {
        this.fabricService.addRegistryPeer(userDetails, request);
        return ResponseEntity.ok().body(Response.success(null));
    }
}
