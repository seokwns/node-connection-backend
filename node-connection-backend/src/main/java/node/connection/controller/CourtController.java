package node.connection.controller;

import node.connection._core.response.Response;
import node.connection._core.security.CustomUserDetails;
import node.connection.dto.court.request.AddCourtMemberRequest;
import node.connection.dto.court.request.CourtCreateRequest;
import node.connection.dto.court.request.DeleteCourtMemberRequest;
import node.connection.dto.court.request.FinalizeCourtRequest;
import node.connection.dto.court.response.FabricCourt;
import node.connection.dto.court.response.FabricCourtRequest;
import node.connection.dto.registry.request.RegistryCreateRequest;
import node.connection.service.CourtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/court")
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

    @PostMapping("")
    public ResponseEntity<?> createNewCourt(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CourtCreateRequest request
    ) {
        this.courtService.createCourt(userDetails, request);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PostMapping("/member")
    public ResponseEntity<?> addCourtMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AddCourtMemberRequest request
    ) {
        this.courtService.addCourtMember(userDetails, request);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @DeleteMapping("/member")
    public ResponseEntity<?> deleteCourtMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody DeleteCourtMemberRequest request
    ) {
        this.courtService.deleteCourtMember(userDetails, request);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @GetMapping("/{id}/request/unfinalized")
    public ResponseEntity<?> getUnfinalizedRequests(@PathVariable("id") String id) {
        List<FabricCourtRequest> requests = this.courtService.getUnfinalizedRequests(id);
        return ResponseEntity.ok().body(Response.success(requests));
    }

    @GetMapping("/{id}/request/finalized")
    public ResponseEntity<?> getFinalizedRequests(@PathVariable("id") String id) {
        List<FabricCourtRequest> requests = this.courtService.getFinalizedRequests(id);
        return ResponseEntity.ok().body(Response.success(requests));
    }

    @GetMapping("/request")
    public ResponseEntity<?> getCourtRequestsByRequestorId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<FabricCourtRequest> requests = this.courtService.getCourtRequestsByRequestorId(userDetails);
        return ResponseEntity.ok().body(Response.success(requests));
    }

    @PostMapping("/{id}/request/registry")
    public ResponseEntity<?> createRegistryCourtRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") String courtId,
            @RequestBody RegistryCreateRequest request
    ) {
        this.courtService.createRegistryCourtRequest(userDetails, courtId, request);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PostMapping("/{id}/request/finalize")
    public ResponseEntity<?> finalizeCourtRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") String courtId,
            @RequestBody FinalizeCourtRequest request
    ) {
        this.courtService.finalizeCourtRequest(userDetails, courtId, request);
        return ResponseEntity.ok().body(Response.success(null));
    }
}
