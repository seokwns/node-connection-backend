package node.connection.controller;

import node.connection._core.response.Response;
import node.connection._core.security.CustomUserDetails;
import node.connection.dto.court.request.AddCourtMemberRequest;
import node.connection.dto.court.request.DeleteCourtMemberRequest;
import node.connection.dto.user.response.UserSearchResponse;
import node.connection.service.CourtService;
import node.connection.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CourtService courtService;

    private final UserService userService;


    public AdminController(
            @Autowired CourtService courtService,
            @Autowired UserService userService
    ) {
        this.courtService = courtService;
        this.userService = userService;
    }

    @PostMapping("/member")
    public ResponseEntity<?> addCourtMember(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @RequestBody AddCourtMemberRequest request
    ) {
        this.courtService.addCourtMember(userDetails, request);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @DeleteMapping("/member")
    public ResponseEntity<?> deleteCourtMember(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @RequestBody DeleteCourtMemberRequest request
    ) {
        this.courtService.deleteCourtMember(userDetails, request);
        return ResponseEntity.ok().body(Response.success(null));
    }
}
