package node.connection.controller;

import node.connection._core.response.Response;
import node.connection._core.security.CustomUserDetails;
import node.connection.dto.court.request.AddCourtMemberRequest;
import node.connection.dto.court.request.CourtCreateRequest;
import node.connection.dto.court.response.FabricCourt;
import node.connection.service.CourtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/court")
public class CourtController {

    private final CourtService courtService;

    public CourtController(@Autowired CourtService courtService) {
        this.courtService = courtService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourtById(@PathVariable("id") String id) {
        FabricCourt court = this.courtService.getCourtById(id);
        return ResponseEntity.ok().body(Response.success(court));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createNewCourt(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CourtCreateRequest request
    ) {
        this.courtService.createCourt(userDetails, request);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PostMapping("/member/add")
    public ResponseEntity<?> addCourtMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AddCourtMemberRequest request
    ) {
        this.courtService.addCourtMember(userDetails, request);
        return ResponseEntity.ok().body(Response.success(null));
    }
}
